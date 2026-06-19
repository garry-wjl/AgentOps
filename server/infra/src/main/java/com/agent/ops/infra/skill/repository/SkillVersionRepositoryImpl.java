package com.agent.ops.infra.skill.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.SkillVersionStatus;
import com.agent.ops.domain.skill.SkillVersionAggregate;
import com.agent.ops.domain.skill.repository.SkillVersionRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.skill.entity.SkillVersionEntity;
import com.agent.ops.infra.skill.mapper.SkillVersionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class SkillVersionRepositoryImpl implements SkillVersionRepository {
    @Resource
    private SkillVersionMapper skillVersionMapper;

    @Override
    public void save(SkillVersionAggregate aggregate) {
        SkillVersionEntity e = toEntity(aggregate);
        SkillVersionEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            skillVersionMapper.insert(e);
            aggregate.setId(e.id);
            return;
        }
        e.id = existing.id;
        e.createNo = existing.createNo;
        e.createTime = existing.createTime;
        skillVersionMapper.updateById(e);
        aggregate.setId(e.id);
    }

    @Override
    public SkillVersionAggregate findByNum(String num) {
        SkillVersionEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SkillVersionEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SkillVersionEntity::getNum, num)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SkillVersionEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SkillVersionEntity::getUpdateNo, operatorCode);
        skillVersionMapper.update(null, wrapper);
    }

    @Override
    public SkillVersionAggregate findEffectiveBySkillCode(String skillCode) {
        if (StrUtil.isBlank(skillCode)) return null;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getStatus, SkillVersionStatus.EFFECTIVE.getCode())
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        SkillVersionEntity e = skillVersionMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public SkillVersionAggregate findDraftBySkillCode(String skillCode) {
        if (StrUtil.isBlank(skillCode)) return null;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getStatus, SkillVersionStatus.DRAFT.getCode())
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        SkillVersionEntity e = skillVersionMapper.selectOne(wrapper);
        return e == null ? null : toDomain(e);
    }

    @Override
    public List<SkillVersionAggregate> listBySkillCode(String skillCode) {
        if (StrUtil.isBlank(skillCode)) return Collections.emptyList();
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(SkillVersionEntity::getCreateTime);
        List<SkillVersionEntity> entities = skillVersionMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<SkillVersionAggregate> list = new ArrayList<>();
        for (SkillVersionEntity e : entities) {
            list.add(toDomain(e));
        }
        return list;
    }

    @Override
    public boolean existsByVersionNo(String skillCode, String versionNo, String excludeNum) {
        if (StrUtil.isBlank(versionNo)) return false;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getVersionNo, versionNo)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SkillVersionEntity::getNum, excludeNum);
        }
        return skillVersionMapper.selectCount(wrapper) > 0;
    }

    private SkillVersionEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getNum, num)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return skillVersionMapper.selectOne(wrapper);
    }

    private SkillVersionEntity toEntity(SkillVersionAggregate a) {
        SkillVersionEntity e = new SkillVersionEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.skillCode = a.getSkillCode();
        e.versionNo = a.getVersionNo();
        e.skillMdContent = a.getSkillMdContent();
        e.status = a.getStatus() == null ? SkillVersionStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.publishTime = a.getPublishTime();
        e.withdrawTime = a.getWithdrawTime();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private SkillVersionAggregate toDomain(SkillVersionEntity e) {
        SkillVersionAggregate a = new SkillVersionAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSkillCode(e.skillCode);
        a.setVersionNo(e.versionNo);
        a.setSkillMdContent(e.skillMdContent);
        a.setStatus(e.status == null ? null : SkillVersionStatus.fromCode(e.status));
        a.setPublishTime(e.publishTime);
        a.setWithdrawTime(e.withdrawTime);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
