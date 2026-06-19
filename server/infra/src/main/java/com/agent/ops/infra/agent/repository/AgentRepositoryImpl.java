package com.agent.ops.infra.agent.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.enums.AgentStatus;
import com.agent.ops.domain.agent.AgentAggregate;
import com.agent.ops.domain.agent.repository.AgentRepository;
import com.agent.ops.infra.agent.entity.AgentEntity;
import com.agent.ops.infra.agent.mapper.AgentMapper;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AgentRepositoryImpl implements AgentRepository {
    @Resource
    private AgentMapper agentMapper;

    @Override
    public void save(AgentAggregate aggregate) {
        AgentEntity e = toEntity(aggregate);
        AgentEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            agentMapper.insert(e);
            aggregate.setId(e.id);
            return;
        }
        e.id = existing.id;
        e.createNo = existing.createNo;
        e.createTime = existing.createTime;
        // name 不可改：强制保留
        e.name = existing.name;
        agentMapper.updateById(e);
        aggregate.setId(e.id);
    }

    @Override
    public AgentAggregate findByNum(String num) {
        AgentEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public AgentAggregate findByName(String spaceCode, String name) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(name)) return null;
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getSpaceCode, spaceCode)
                .eq(AgentEntity::getName, name)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        AgentEntity e = agentMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<AgentEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AgentEntity::getNum, num)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(AgentEntity::getIsDeleted, InfraConstant.DELETED)
                .set(AgentEntity::getUpdateNo, operatorCode);
        agentMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) return false;
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getSpaceCode, spaceCode)
                .eq(AgentEntity::getName, name)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(AgentEntity::getNum, excludeNum);
        }
        return agentMapper.selectCount(wrapper) > 0;
    }

    private AgentEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentEntity::getNum, num)
                .eq(AgentEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return agentMapper.selectOne(wrapper);
    }

    private AgentEntity toEntity(AgentAggregate a) {
        AgentEntity e = new AgentEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.displayName = a.getDisplayName();
        e.description = a.getDescription();
        e.currentVersionNo = a.getCurrentVersionNo();
        e.status = a.getStatus() == null ? AgentStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.tagsJson = JSON.toJSONString(a.getTags());
        e.remark = a.getRemark();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private AgentAggregate toDomain(AgentEntity e) {
        AgentAggregate a = new AgentAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setDisplayName(e.displayName);
        a.setDescription(e.description);
        a.setCurrentVersionNo(e.currentVersionNo);
        a.setStatus(e.status == null ? null : AgentStatus.fromCode(e.status));
        a.setTags(parseTags(e.tagsJson));
        a.setRemark(e.remark);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }

    private List<String> parseTags(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : list;
    }
}
