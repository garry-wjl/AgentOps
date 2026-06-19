package com.agent.ops.application.agent.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.dto.AgentDTO;
import com.agent.ops.client.agent.dto.AgentVersionDTO;
import com.agent.ops.client.agent.dto.AssemblySnapshotDTO;
import com.agent.ops.client.agent.enums.AgentStatus;
import com.agent.ops.client.agent.enums.AgentVersionStatus;
import com.agent.ops.client.agent.param.AgentQueryParam;
import com.agent.ops.client.agent.vo.AgentVO;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.agent.entity.AgentEntity;
import com.agent.ops.infra.agent.entity.AgentVersionEntity;
import com.agent.ops.infra.agent.mapper.AgentMapper;
import com.agent.ops.infra.agent.mapper.AgentVersionMapper;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AgentQueryService {
    @Resource
    private AgentMapper agentMapper;

    @Resource
    private AgentVersionMapper agentVersionMapper;

    public AgentDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        AgentEntity e = findEntityByNum(num);
        return e == null ? null : toDTO(e);
    }

    public PageResult<AgentVO> page(AgentQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getSpaceCode, param.spaceCode)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(AgentEntity::getName, param.keyword)
                    .or().like(AgentEntity::getDisplayName, param.keyword)
                    .or().like(AgentEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            AgentStatus s = AgentStatus.valueOf(param.status);
            wrapper.eq(AgentEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(AgentEntity::getUpdateTime);

        IPage<AgentEntity> page = new Page<>(pageNo, pageSize);
        IPage<AgentEntity> result = agentMapper.selectPage(page, wrapper);
        List<AgentVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (AgentEntity e : result.getRecords()) {
                records.add(toVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    /**
     * 运行时入口：按 (spaceCode, name) 取当前在线版本快照。
     *
     * @param spaceCode 空间业务编码
     * @param name      Agent 名称（英文）
     * @return 版本 DTO，前提是 主体.status=EFFECTIVE 且存在 ONLINE 版本
     */
    public AgentVersionDTO getOnlineByName(String spaceCode, String name) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(name)) return null;
        LambdaQueryWrapper<AgentEntity> aw = new LambdaQueryWrapper<>();
        aw.eq(AgentEntity::getSpaceCode, spaceCode)
                .eq(AgentEntity::getName, name)
                .eq(AgentEntity::getStatus, AgentStatus.EFFECTIVE.getCode())
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentEntity agent = agentMapper.selectOne(aw);
        if (agent == null) return null;
        LambdaQueryWrapper<AgentVersionEntity> vw = new LambdaQueryWrapper<>();
        vw.eq(AgentVersionEntity::getAgentCode, agent.num)
                .eq(AgentVersionEntity::getStatus, AgentVersionStatus.ONLINE.getCode())
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentVersionEntity v = agentVersionMapper.selectOne(vw);
        return v == null ? null : toVersionDTO(v);
    }

    public List<AgentVersionDTO> listVersionsByAgentCode(String agentCode) {
        if (StrUtil.isBlank(agentCode)) return Collections.emptyList();
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentCode, agentCode)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(AgentVersionEntity::getCreateTime);
        List<AgentVersionEntity> entities = agentVersionMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<AgentVersionDTO> list = new ArrayList<>();
        for (AgentVersionEntity e : entities) {
            list.add(toVersionDTO(e));
        }
        return list;
    }

    public AgentVersionDTO getVersionByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getNum, num)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentVersionEntity e = agentVersionMapper.selectOne(wrapper);
        return e == null ? null : toVersionDTO(e);
    }

    private AgentEntity findEntityByNum(String num) {
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getNum, num)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return agentMapper.selectOne(wrapper);
    }

    private AgentDTO toDTO(AgentEntity e) {
        AgentDTO dto = new AgentDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.displayName = e.displayName;
        dto.description = e.description;
        dto.currentVersionNo = e.currentVersionNo;
        dto.status = e.status == null ? null : AgentStatus.fromCode(e.status);
        dto.tags = parseTags(e.tagsJson);
        dto.remark = e.remark;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private AgentVO toVO(AgentEntity e) {
        AgentVO vo = new AgentVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.displayName = e.displayName;
        vo.description = e.description;
        vo.currentVersionNo = e.currentVersionNo;
        vo.status = e.status == null ? null : AgentStatus.fromCode(e.status);
        vo.tags = parseTags(e.tagsJson);
        vo.updateTime = e.updateTime;
        return vo;
    }

    private AgentVersionDTO toVersionDTO(AgentVersionEntity e) {
        AgentVersionDTO dto = new AgentVersionDTO();
        dto.num = e.num;
        dto.agentCode = e.agentCode;
        dto.versionNo = e.versionNo;
        dto.snapshot = parseSnapshot(e.assemblySnapshot);
        dto.status = e.status == null ? null : AgentVersionStatus.fromCode(e.status);
        dto.onlineTime = e.onlineTime;
        dto.offlineTime = e.offlineTime;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private AssemblySnapshotDTO parseSnapshot(String json) {
        if (StrUtil.isBlank(json)) return new AssemblySnapshotDTO();
        AssemblySnapshot s = JSON.parseObject(json, AssemblySnapshot.class);
        if (s == null) return new AssemblySnapshotDTO();
        AssemblySnapshotDTO dto = new AssemblySnapshotDTO();
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

    private List<String> parseTags(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : list;
    }
}
