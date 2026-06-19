package com.agent.ops.infra.model.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.model.enums.ModelStatus;
import com.agent.ops.domain.model.ModelAggregate;
import com.agent.ops.domain.model.repository.ModelRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.model.entity.ModelEntity;
import com.agent.ops.infra.model.mapper.ModelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 模型仓储实现。
 */
@Repository
public class ModelRepositoryImpl implements ModelRepository {
    @Resource
    private ModelMapper modelMapper;

    @Override
    public void save(ModelAggregate aggregate) {
        ModelEntity entity = toEntity(aggregate);
        ModelEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            modelMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        modelMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    @Override
    public ModelAggregate findByNum(String num) {
        ModelEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<ModelEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelEntity::getNum, num)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(ModelEntity::getIsDeleted, InfraConstant.DELETED)
                .set(ModelEntity::getUpdateNo, operatorCode);
        modelMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getSpaceCode, spaceCode)
                .eq(ModelEntity::getName, name)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(ModelEntity::getNum, excludeNum);
        }
        return modelMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByModelId(String spaceCode, String modelId, String excludeNum) {
        if (StrUtil.isBlank(modelId)) {
            return false;
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getSpaceCode, spaceCode)
                .eq(ModelEntity::getModelId, modelId)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(ModelEntity::getNum, excludeNum);
        }
        return modelMapper.selectCount(wrapper) > 0;
    }

    private ModelEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getNum, num)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return modelMapper.selectOne(wrapper);
    }

    private ModelEntity toEntity(ModelAggregate a) {
        ModelEntity e = new ModelEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.modelId = a.getModelId();
        e.baseUrl = a.getBaseUrl();
        e.apiKeyCipher = a.getApiKeyCipher();
        e.remark = a.getRemark();
        e.status = a.getStatus() == null ? ModelStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private ModelAggregate toDomain(ModelEntity e) {
        ModelAggregate a = new ModelAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setModelId(e.modelId);
        a.setBaseUrl(e.baseUrl);
        a.setApiKeyCipher(e.apiKeyCipher);
        a.setRemark(e.remark);
        a.setStatus(e.status == null ? null : ModelStatus.fromCode(e.status));
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
