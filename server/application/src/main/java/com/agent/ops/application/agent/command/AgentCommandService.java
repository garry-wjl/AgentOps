package com.agent.ops.application.agent.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.dto.AgentDTO;
import com.agent.ops.client.agent.dto.AgentVersionDTO;
import com.agent.ops.client.agent.dto.AssemblySnapshotDTO;
import com.agent.ops.client.agent.param.AgentActionParam;
import com.agent.ops.client.agent.param.CreateAgentParam;
import com.agent.ops.client.agent.param.UpdateAgentBasicParam;
import com.agent.ops.domain.agent.AgentAggregate;
import com.agent.ops.domain.agent.factory.AgentFactory;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentCommandService {
    @Resource
    private AgentFactory agentFactory;

    @Resource
    private AgentVersionCommandService agentVersionCommandService;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public AgentDTO create(CreateAgentParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        Assert.notBlank(param.name, "name 不能为空");
        return distributedLock.execute("agent:create:" + param.spaceCode + ":" + param.name, () -> {
            AgentAggregate agent = agentFactory.create(param.spaceCode, param.name, param.displayName, param.description, param.tags, param.remark);
            agent.save(param.getOperatorCode());
            // 自动创建 V1 草稿版本
            String versionNo = StrUtil.blankToDefault(param.versionNo, "1.0.0");
            AssemblySnapshotDTO assembly = param.initialAssembly == null ? new AssemblySnapshotDTO() : param.initialAssembly;
            AgentVersionDTO versionDto = agentVersionCommandService.createInitialVersion(agent.getNum(), versionNo, assembly, param.getOperatorCode());
            AgentDTO dto = toDTO(agent);
            dto.currentVersionNo = versionDto.versionNo;
            return dto;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentDTO updateBasic(UpdateAgentBasicParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("agent:" + param.num, () -> {
            AgentAggregate agent = loadAggregate(param.num);
            if (param.displayName != null) agent.setDisplayName(param.displayName);
            if (param.description != null) agent.setDescription(param.description);
            if (param.tags != null) agent.setTags(param.tags);
            if (param.remark != null) agent.setRemark(param.remark);
            agent.save(param.getOperatorCode());
            return toDTO(agent);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentDTO enable(AgentActionParam param) {
        return distributedLock.execute("agent:" + param.num, () -> {
            AgentAggregate agent = loadAggregate(param.num);
            agent.enable(param.getOperatorCode());
            return toDTO(agent);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentDTO withdraw(AgentActionParam param) {
        return distributedLock.execute("agent:" + param.num, () -> {
            AgentAggregate agent = loadAggregate(param.num);
            agent.withdraw(param.getOperatorCode());
            return toDTO(agent);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(AgentActionParam param) {
        distributedLock.run("agent:" + param.num, () -> {
            AgentAggregate agent = loadAggregate(param.num);
            agent.delete(param.getOperatorCode());
        });
    }

    /**
     * 由 listener 调用，刷新主体当前版本号。
     */
    @Transactional(rollbackFor = Exception.class)
    public void refreshCurrentVersion(String agentCode, String versionNo, String operatorCode) {
        AgentAggregate agent = agentFactory.createByNum(agentCode);
        if (agent == null) return;
        agent.setCurrentVersionNo(versionNo);
        agent.save(operatorCode);
    }

    private AgentAggregate loadAggregate(String num) {
        AgentAggregate agent = agentFactory.createByNum(num);
        if (agent == null) {
            throw new BusinessException("AGENT_NOT_FOUND", "Agent 不存在");
        }
        return agent;
    }

    private AgentDTO toDTO(AgentAggregate a) {
        AgentDTO dto = new AgentDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.displayName = a.getDisplayName();
        dto.description = a.getDescription();
        dto.currentVersionNo = a.getCurrentVersionNo();
        dto.status = a.getStatus();
        dto.tags = a.getTags();
        dto.remark = a.getRemark();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
