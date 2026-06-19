package com.agent.ops.infra.skill.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.SkillStatus;
import com.agent.ops.domain.skill.SkillAggregate;
import com.agent.ops.domain.skill.repository.SkillRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.skill.entity.SkillEntity;
import com.agent.ops.infra.skill.mapper.SkillMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SkillRepositoryImpl implements SkillRepository {
    @Resource
    private SkillMapper skillMapper;

    @Override
    public void save(SkillAggregate aggregate) {
        SkillEntity e = toEntity(aggregate);
        SkillEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            skillMapper.insert(e);
            aggregate.setId(e.id);
            return;
        }
        e.id = existing.id;
        e.createNo = existing.createNo;
        e.createTime = existing.createTime;
        skillMapper.updateById(e);
        aggregate.setId(e.id);
    }

    @Override
    public SkillAggregate findByNum(String num) {
        SkillEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SkillEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SkillEntity::getNum, num)
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SkillEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SkillEntity::getUpdateNo, operatorCode);
        skillMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) return false;
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getSpaceCode, spaceCode)
                .eq(SkillEntity::getName, name)
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SkillEntity::getNum, excludeNum);
        }
        return skillMapper.selectCount(wrapper) > 0;
    }

    private SkillEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getNum, num)
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return skillMapper.selectOne(wrapper);
    }

    private SkillEntity toEntity(SkillAggregate a) {
        SkillEntity e = new SkillEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.description = a.getDescription();
        e.currentVersionNo = a.getCurrentVersionNo();
        e.status = a.getStatus() == null ? SkillStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.tagsJson = JSON.toJSONString(a.getTags());
        e.remark = a.getRemark();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private SkillAggregate toDomain(SkillEntity e) {
        SkillAggregate a = new SkillAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setDescription(e.description);
        a.setCurrentVersionNo(e.currentVersionNo);
        a.setStatus(e.status == null ? null : SkillStatus.fromCode(e.status));
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
