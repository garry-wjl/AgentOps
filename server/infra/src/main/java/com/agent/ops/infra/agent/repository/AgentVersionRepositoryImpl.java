package com.agent.ops.infra.agent.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.enums.AgentVersionStatus;
import com.agent.ops.domain.agent.AgentVersionAggregate;
import com.agent.ops.domain.agent.repository.AgentVersionRepository;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import com.agent.ops.infra.agent.entity.AgentVersionEntity;
import com.agent.ops.infra.agent.entity.AgentVersionSkillRefEntity;
import com.agent.ops.infra.agent.entity.AgentVersionToolRefEntity;
import com.agent.ops.infra.agent.mapper.AgentVersionMapper;
import com.agent.ops.infra.agent.mapper.AgentVersionSkillRefMapper;
import com.agent.ops.infra.agent.mapper.AgentVersionToolRefMapper;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class AgentVersionRepositoryImpl implements AgentVersionRepository {
    @Resource
    private AgentVersionMapper agentVersionMapper;

    @Resource
    private AgentVersionSkillRefMapper agentVersionSkillRefMapper;

    @Resource
    private AgentVersionToolRefMapper agentVersionToolRefMapper;

    @Override
    public void save(AgentVersionAggregate aggregate) {
        AgentVersionEntity e = toEntity(aggregate);
        AgentVersionEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            agentVersionMapper.insert(e);
            aggregate.setId(e.id);
        } else {
            e.id = existing.id;
            e.createNo = existing.createNo;
            e.createTime = existing.createTime;
            agentVersionMapper.updateById(e);
            aggregate.setId(e.id);
        }
        // 同步 ref 子表
        rebuildRefs(aggregate);
    }

    private void rebuildRefs(AgentVersionAggregate aggregate) {
        String versionCode = aggregate.getNum();
        // 先清空旧的（软删）
        LambdaUpdateWrapper<AgentVersionSkillRefEntity> sw = new LambdaUpdateWrapper<>();
        sw.eq(AgentVersionSkillRefEntity::getAgentVersionCode, versionCode)
                .eq(AgentVersionSkillRefEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(AgentVersionSkillRefEntity::getIsDeleted, InfraConstant.DELETED);
        agentVersionSkillRefMapper.update(null, sw);

        LambdaUpdateWrapper<AgentVersionToolRefEntity> tw = new LambdaUpdateWrapper<>();
        tw.eq(AgentVersionToolRefEntity::getAgentVersionCode, versionCode)
                .eq(AgentVersionToolRefEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(AgentVersionToolRefEntity::getIsDeleted, InfraConstant.DELETED);
        agentVersionToolRefMapper.update(null, tw);

        // 重建
        AssemblySnapshot snapshot = aggregate.getSnapshot();
        if (snapshot == null) return;
        if (CollUtil.isNotEmpty(snapshot.getSkillCodes())) {
            for (String code : snapshot.getSkillCodes()) {
                AgentVersionSkillRefEntity ref = new AgentVersionSkillRefEntity();
                ref.agentVersionCode = versionCode;
                ref.skillCode = code;
                ref.createNo = aggregate.getCreateNo();
                ref.updateNo = aggregate.getUpdateNo();
                ref.isDeleted = InfraConstant.NOT_DELETED;
                agentVersionSkillRefMapper.insert(ref);
            }
        }
        if (CollUtil.isNotEmpty(snapshot.getToolCodes())) {
            for (String code : snapshot.getToolCodes()) {
                AgentVersionToolRefEntity ref = new AgentVersionToolRefEntity();
                ref.agentVersionCode = versionCode;
                ref.toolCode = code;
                ref.createNo = aggregate.getCreateNo();
                ref.updateNo = aggregate.getUpdateNo();
                ref.isDeleted = InfraConstant.NOT_DELETED;
                agentVersionToolRefMapper.insert(ref);
            }
        }
    }

    @Override
    public AgentVersionAggregate findByNum(String num) {
        AgentVersionEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<AgentVersionEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AgentVersionEntity::getNum, num)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(AgentVersionEntity::getIsDeleted, InfraConstant.DELETED)
                .set(AgentVersionEntity::getUpdateNo, operatorCode);
        agentVersionMapper.update(null, wrapper);
    }

    @Override
    public AgentVersionAggregate findOnlineByAgentCode(String agentCode) {
        if (StrUtil.isBlank(agentCode)) return null;
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentCode, agentCode)
                .eq(AgentVersionEntity::getStatus, AgentVersionStatus.ONLINE.getCode())
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentVersionEntity e = agentVersionMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public AgentVersionAggregate findDraftByAgentCode(String agentCode) {
        if (StrUtil.isBlank(agentCode)) return null;
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentCode, agentCode)
                .eq(AgentVersionEntity::getStatus, AgentVersionStatus.DRAFT.getCode())
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentVersionEntity e = agentVersionMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public List<AgentVersionAggregate> listByAgentCode(String agentCode) {
        if (StrUtil.isBlank(agentCode)) return Collections.emptyList();
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentCode, agentCode)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(AgentVersionEntity::getCreateTime);
        List<AgentVersionEntity> entities = agentVersionMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<AgentVersionAggregate> list = new ArrayList<>();
        for (AgentVersionEntity e : entities) {
            list.add(toDomain(e));
        }
        return list;
    }

    @Override
    public boolean existsByVersionNo(String agentCode, String versionNo, String excludeNum) {
        if (StrUtil.isBlank(versionNo)) return false;
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getAgentCode, agentCode)
                .eq(AgentVersionEntity::getVersionNo, versionNo)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(AgentVersionEntity::getNum, excludeNum);
        }
        return agentVersionMapper.selectCount(wrapper) > 0;
    }

    private AgentVersionEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<AgentVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentVersionEntity::getNum, num)
                .eq(AgentVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return agentVersionMapper.selectOne(wrapper);
    }

    private AgentVersionEntity toEntity(AgentVersionAggregate a) {
        AgentVersionEntity e = new AgentVersionEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.agentCode = a.getAgentCode();
        e.versionNo = a.getVersionNo();
        e.assemblySnapshot = a.getSnapshot() == null ? null : JSON.toJSONString(a.getSnapshot());
        e.status = a.getStatus() == null ? AgentVersionStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.onlineTime = a.getOnlineTime();
        e.offlineTime = a.getOfflineTime();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private AgentVersionAggregate toDomain(AgentVersionEntity e) {
        AgentVersionAggregate a = new AgentVersionAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setAgentCode(e.agentCode);
        a.setVersionNo(e.versionNo);
        a.setSnapshot(StrUtil.isBlank(e.assemblySnapshot)
                ? new AssemblySnapshot() : JSON.parseObject(e.assemblySnapshot, AssemblySnapshot.class));
        a.setStatus(e.status == null ? null : AgentVersionStatus.fromCode(e.status));
        a.setOnlineTime(e.onlineTime);
        a.setOfflineTime(e.offlineTime);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
