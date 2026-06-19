package com.agent.ops.infra.prompt.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.prompt.enums.PromptStatus;
import com.agent.ops.domain.prompt.PromptAggregate;
import com.agent.ops.domain.prompt.repository.PromptRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.prompt.entity.PromptEntity;
import com.agent.ops.infra.prompt.mapper.PromptMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PromptRepositoryImpl implements PromptRepository {
    @Resource
    private PromptMapper promptMapper;

    @Override
    public void save(PromptAggregate aggregate) {
        PromptEntity entity = toEntity(aggregate);
        PromptEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            promptMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        promptMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    @Override
    public PromptAggregate findByNum(String num) {
        PromptEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public PromptAggregate findByKey(String spaceCode, String key) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(key)) {
            return null;
        }
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, spaceCode)
                .eq(PromptEntity::getKey, key)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        PromptEntity e = promptMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<PromptEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PromptEntity::getNum, num)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(PromptEntity::getIsDeleted, InfraConstant.DELETED)
                .set(PromptEntity::getUpdateNo, operatorCode);
        promptMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) return false;
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, spaceCode)
                .eq(PromptEntity::getName, name)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(PromptEntity::getNum, excludeNum);
        }
        return promptMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByKey(String spaceCode, String key, String excludeNum) {
        if (StrUtil.isBlank(key)) return false;
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, spaceCode)
                .eq(PromptEntity::getKey, key)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(PromptEntity::getNum, excludeNum);
        }
        return promptMapper.selectCount(wrapper) > 0;
    }

    private PromptEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getNum, num)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return promptMapper.selectOne(wrapper);
    }

    private PromptEntity toEntity(PromptAggregate a) {
        PromptEntity e = new PromptEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.key = a.getKey();
        e.content = a.getContent();
        e.variablesJson = JSON.toJSONString(a.getVariables() == null ? new ArrayList<String>() : a.getVariables());
        e.remark = a.getRemark();
        e.status = a.getStatus() == null ? PromptStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private PromptAggregate toDomain(PromptEntity e) {
        PromptAggregate a = new PromptAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setKey(e.key);
        a.setContent(e.content);
        a.setVariables(parseVariables(e.variablesJson));
        a.setRemark(e.remark);
        a.setStatus(e.status == null ? null : PromptStatus.fromCode(e.status));
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }

    private List<String> parseVariables(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }
}
