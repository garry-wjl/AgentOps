package com.agent.ops.infra.tool.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.domain.tool.ToolAggregate;
import com.agent.ops.domain.tool.repository.ToolRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.tool.entity.ToolEntity;
import com.agent.ops.infra.tool.mapper.ToolMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ToolRepositoryImpl implements ToolRepository {
    @Resource
    private ToolMapper toolMapper;

    @Override
    public void save(ToolAggregate aggregate) {
        ToolEntity entity = toEntity(aggregate);
        ToolEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            toolMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        // type/subType 不允许变更，强制保留原值
        entity.type = existing.type;
        entity.subType = existing.subType;
        toolMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    @Override
    public ToolAggregate findByNum(String num) {
        ToolEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<ToolEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ToolEntity::getNum, num)
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(ToolEntity::getIsDeleted, InfraConstant.DELETED)
                .set(ToolEntity::getUpdateNo, operatorCode);
        toolMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) return false;
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getSpaceCode, spaceCode)
                .eq(ToolEntity::getName, name)
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(ToolEntity::getNum, excludeNum);
        }
        return toolMapper.selectCount(wrapper) > 0;
    }

    private ToolEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getNum, num)
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return toolMapper.selectOne(wrapper);
    }

    private ToolEntity toEntity(ToolAggregate a) {
        ToolEntity e = new ToolEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.type = a.getType() == null ? null : a.getType().name();
        e.subType = a.getSubType() == null ? null : a.getSubType().name();
        e.description = a.getDescription();
        e.tagsJson = JSON.toJSONString(a.getTags());
        e.configJson = a.getConfigJson();
        e.status = a.getStatus() == null ? ToolStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.remark = a.getRemark();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private ToolAggregate toDomain(ToolEntity e) {
        ToolAggregate a = new ToolAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setType(e.type == null ? null : ToolType.valueOf(e.type));
        a.setSubType(e.subType == null ? null : ToolSubType.valueOf(e.subType));
        a.setDescription(e.description);
        a.setTags(parseTags(e.tagsJson));
        a.setConfigJson(e.configJson);
        a.setStatus(e.status == null ? null : ToolStatus.fromCode(e.status));
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
