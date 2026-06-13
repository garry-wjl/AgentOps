package com.agent.ops.application.agent.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.agent.assembler.AssemblyValidator;
import com.agent.ops.application.prompt.query.PromptQueryService;
import com.agent.ops.client.agent.dto.AgentVersionDTO;
import com.agent.ops.client.agent.dto.AssemblySnapshotDTO;
import com.agent.ops.client.agent.param.AgentActionParam;
import com.agent.ops.client.agent.param.DeriveAgentVersionParam;
import com.agent.ops.client.agent.param.EditAssemblyParam;
import com.agent.ops.client.agent.vo.PrePublishCheckVO;
import com.agent.ops.client.prompt.dto.PromptDTO;
import com.agent.ops.domain.agent.AgentAggregate;
import com.agent.ops.domain.agent.AgentVersionAggregate;
import com.agent.ops.domain.agent.factory.AgentFactory;
import com.agent.ops.domain.agent.factory.AgentVersionFactory;
import com.agent.ops.domain.agent.repository.AgentVersionRepository;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentVersionCommandService {
    @Resource
    private AgentVersionFactory agentVersionFactory;

    @Resource
    private AgentFactory agentFactory;

    @Resource
    private AgentVersionRepository agentVersionRepository;

    @Resource
    private AssemblyValidator assemblyValidator;

    @Resource
    private PromptQueryService promptQueryService;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 创建初始 V1 版本（仅 AgentCommandService 内部调用）。
     */
    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDTO createInitialVersion(String agentCode, String versionNo, AssemblySnapshotDTO assembly, String operatorCode) {
        AssemblySnapshot snapshot = toDomain(assembly);
        // 处理 systemPromptSourceCode 自动填充
        autoFillFromSourcePrompt(snapshot);
        AgentVersionAggregate v = agentVersionFactory.create(agentCode, versionNo, snapshot);
        v.save(operatorCode);
        return toDTO(v);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDTO deriveDraft(DeriveAgentVersionParam param) {
        Assert.notBlank(param.agentCode, "agentCode 不能为空");
        Assert.notBlank(param.sourceVersionCode, "sourceVersionCode 不能为空");
        Assert.notBlank(param.newVersionNo, "newVersionNo 不能为空");
        return distributedLock.execute("agent_version:derive:" + param.agentCode, () -> {
            // 同一 Agent 同时只能有一个 DRAFT
            AgentVersionAggregate existingDraft = agentVersionRepository.findDraftByAgentCode(param.agentCode);
            if (existingDraft != null) {
                return toDTO(existingDraft);
            }
            AgentVersionAggregate source = agentVersionFactory.createByNum(param.sourceVersionCode);
            if (source == null) {
                throw new BusinessException("AGENT_VERSION_NOT_FOUND", "源版本不存在");
            }
            AgentVersionAggregate v = agentVersionFactory.create(param.agentCode, param.newVersionNo, source.getSnapshot());
            v.save(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDTO editAssembly(EditAssemblyParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("agent_version:" + param.num, () -> {
            AgentVersionAggregate v = loadAggregate(param.num);
            AssemblySnapshot snapshot = toDomain(param.snapshot);
            autoFillFromSourcePrompt(snapshot);
            v.setSnapshot(snapshot);
            v.save(param.getOperatorCode());
            return toDTO(v);
        });
    }

    public PrePublishCheckVO prePublishCheck(String num) {
        Assert.notBlank(num, "num 不能为空");
        AgentVersionAggregate v = agentVersionFactory.createByNum(num);
        if (v == null) {
            throw new BusinessException("AGENT_VERSION_NOT_FOUND", "Agent 版本不存在");
        }
        AgentAggregate agent = agentFactory.createByNum(v.getAgentCode());
        String spaceCode = agent == null ? null : agent.getSpaceCode();
        return assemblyValidator.validate(v.getSnapshot(), spaceCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDTO publish(AgentActionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("agent_version:" + param.num, () -> {
            AgentVersionAggregate v = loadAggregate(param.num);
            AgentAggregate agent = agentFactory.createByNum(v.getAgentCode());
            String spaceCode = agent == null ? null : agent.getSpaceCode();
            PrePublishCheckVO check = assemblyValidator.validate(v.getSnapshot(), spaceCode);
            if (Boolean.FALSE.equals(check.passed)) {
                StringBuilder msg = new StringBuilder("装配预检不通过：");
                for (PrePublishCheckVO.Item item : check.errors) {
                    msg.append(item.message).append("; ");
                }
                throw new BusinessException("PRE_PUBLISH_FAILED", msg.toString());
            }
            v.publish(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentVersionDTO offline(AgentActionParam param) {
        return distributedLock.execute("agent_version:" + param.num, () -> {
            AgentVersionAggregate v = loadAggregate(param.num);
            v.offline(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(AgentActionParam param) {
        distributedLock.run("agent_version:" + param.num, () -> {
            AgentVersionAggregate v = loadAggregate(param.num);
            v.delete(param.getOperatorCode());
        });
    }

    /**
     * 当 systemPromptSourceCode / userPromptSourceCode 非空且对应 content 为空时，
     * 从 PromptQueryService 拉取并自动填充 content（即"选入即复制"）。
     *
     * @param snapshot 装配快照
     */
    private void autoFillFromSourcePrompt(AssemblySnapshot snapshot) {
        if (snapshot == null) return;
        if (StrUtil.isNotBlank(snapshot.getSystemPromptSourceCode())
                && StrUtil.isBlank(snapshot.getSystemPromptContent())) {
            PromptDTO prompt = promptQueryService.getByNum(snapshot.getSystemPromptSourceCode());
            if (prompt != null && StrUtil.isNotBlank(prompt.content)) {
                snapshot.setSystemPromptContent(prompt.content);
            }
        }
        if (StrUtil.isNotBlank(snapshot.getUserPromptSourceCode())
                && StrUtil.isBlank(snapshot.getUserPromptContent())) {
            PromptDTO prompt = promptQueryService.getByNum(snapshot.getUserPromptSourceCode());
            if (prompt != null && StrUtil.isNotBlank(prompt.content)) {
                snapshot.setUserPromptContent(prompt.content);
            }
        }
    }

    private AgentVersionAggregate loadAggregate(String num) {
        AgentVersionAggregate v = agentVersionFactory.createByNum(num);
        if (v == null) {
            throw new BusinessException("AGENT_VERSION_NOT_FOUND", "Agent 版本不存在");
        }
        return v;
    }

    private AssemblySnapshot toDomain(AssemblySnapshotDTO dto) {
        if (dto == null) return new AssemblySnapshot();
        AssemblySnapshot s = new AssemblySnapshot();
        s.setModelCode(dto.modelCode);
        s.setModelParamsJson(dto.modelParamsJson);
        s.setSystemPromptContent(dto.systemPromptContent);
        s.setSystemPromptSourceCode(dto.systemPromptSourceCode);
        s.setUserPromptContent(dto.userPromptContent);
        s.setUserPromptSourceCode(dto.userPromptSourceCode);
        s.setSkillCodes(dto.skillCodes);
        s.setToolCodes(dto.toolCodes);
        s.setSandboxCode(dto.sandboxCode);
        s.setShortMemoryTurns(dto.shortMemoryTurns);
        return s;
    }

    private AssemblySnapshotDTO toDTO(AssemblySnapshot s) {
        AssemblySnapshotDTO dto = new AssemblySnapshotDTO();
        if (s == null) return dto;
        dto.modelCode = s.getModelCode();
        dto.modelParamsJson = s.getModelParamsJson();
        dto.systemPromptContent = s.getSystemPromptContent();
        dto.systemPromptSourceCode = s.getSystemPromptSourceCode();
        dto.userPromptContent = s.getUserPromptContent();
        dto.userPromptSourceCode = s.getUserPromptSourceCode();
        dto.skillCodes = s.getSkillCodes();
        dto.toolCodes = s.getToolCodes();
        dto.sandboxCode = s.getSandboxCode();
        dto.shortMemoryTurns = s.getShortMemoryTurns();
        return dto;
    }

    private AgentVersionDTO toDTO(AgentVersionAggregate v) {
        AgentVersionDTO dto = new AgentVersionDTO();
        dto.num = v.getNum();
        dto.agentCode = v.getAgentCode();
        dto.versionNo = v.getVersionNo();
        dto.snapshot = toDTO(v.getSnapshot());
        dto.status = v.getStatus();
        dto.onlineTime = v.getOnlineTime();
        dto.offlineTime = v.getOfflineTime();
        dto.createTime = v.getCreateTime();
        dto.updateTime = v.getUpdateTime();
        return dto;
    }
}
