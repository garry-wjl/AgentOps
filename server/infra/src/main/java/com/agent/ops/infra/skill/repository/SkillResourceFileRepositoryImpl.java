package com.agent.ops.infra.skill.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.domain.skill.SkillResourceFileAggregate;
import com.agent.ops.domain.skill.repository.SkillResourceFileRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.skill.entity.SkillResourceFileEntity;
import com.agent.ops.infra.skill.mapper.SkillResourceFileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class SkillResourceFileRepositoryImpl implements SkillResourceFileRepository {
    @Resource
    private SkillResourceFileMapper skillResourceFileMapper;

    @Override
    public void save(SkillResourceFileAggregate aggregate) {
        SkillResourceFileEntity e = toEntity(aggregate);
        SkillResourceFileEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            skillResourceFileMapper.insert(e);
            aggregate.setId(e.id);
            return;
        }
        e.id = existing.id;
        e.createNo = existing.createNo;
        e.createTime = existing.createTime;
        skillResourceFileMapper.updateById(e);
        aggregate.setId(e.id);
    }

    @Override
    public SkillResourceFileAggregate findByNum(String num) {
        SkillResourceFileEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SkillResourceFileEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SkillResourceFileEntity::getNum, num)
                .eq(SkillResourceFileEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SkillResourceFileEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SkillResourceFileEntity::getUpdateNo, operatorCode);
        skillResourceFileMapper.update(null, wrapper);
    }

    @Override
    public List<SkillResourceFileAggregate> listByVersionCode(String versionCode) {
        if (StrUtil.isBlank(versionCode)) return Collections.emptyList();
        LambdaQueryWrapper<SkillResourceFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillResourceFileEntity::getSkillVersionCode, versionCode)
                .eq(SkillResourceFileEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByAsc(SkillResourceFileEntity::getPath);
        List<SkillResourceFileEntity> entities = skillResourceFileMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<SkillResourceFileAggregate> list = new ArrayList<>();
        for (SkillResourceFileEntity e : entities) {
            list.add(toDomain(e));
        }
        return list;
    }

    @Override
    public boolean existsByPath(String versionCode, String path, String excludeNum) {
        if (StrUtil.isBlank(path)) return false;
        LambdaQueryWrapper<SkillResourceFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillResourceFileEntity::getSkillVersionCode, versionCode)
                .eq(SkillResourceFileEntity::getPath, path)
                .eq(SkillResourceFileEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SkillResourceFileEntity::getNum, excludeNum);
        }
        return skillResourceFileMapper.selectCount(wrapper) > 0;
    }

    private SkillResourceFileEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<SkillResourceFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillResourceFileEntity::getNum, num)
                .eq(SkillResourceFileEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return skillResourceFileMapper.selectOne(wrapper);
    }

    private SkillResourceFileEntity toEntity(SkillResourceFileAggregate a) {
        SkillResourceFileEntity e = new SkillResourceFileEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.skillVersionCode = a.getSkillVersionCode();
        e.path = a.getPath();
        e.type = a.getType() == null ? null : a.getType().getCode();
        e.content = a.getContent();
        e.sizeBytes = a.getSizeBytes();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private SkillResourceFileAggregate toDomain(SkillResourceFileEntity e) {
        SkillResourceFileAggregate a = new SkillResourceFileAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSkillVersionCode(e.skillVersionCode);
        a.setPath(e.path);
        a.setType(e.type == null ? null : FileType.fromCode(e.type));
        a.setContent(e.content);
        a.setSizeBytes(e.sizeBytes);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
