package com.agent.ops.infra.system.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.repository.SystemSettingRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.system.entity.SystemSettingEntity;
import com.agent.ops.infra.system.mapper.SystemSettingMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 系统设置仓储实现。
 */
@Repository
public class SystemSettingRepositoryImpl implements SystemSettingRepository {
    /**
     * 系统设置 Mapper。
     */
    @Resource
    private SystemSettingMapper systemSettingMapper;

    /**
     * 持久化。
     *
     * @param aggregate 聚合
     */
    @Override
    public void save(SystemSettingAggregate aggregate) {
        SystemSettingEntity entity = toEntity(aggregate);
        SystemSettingEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            systemSettingMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        systemSettingMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    /**
     * 按业务编码查询。
     *
     * @param num 业务编码
     * @return 聚合
     */
    @Override
    public SystemSettingAggregate findByNum(String num) {
        SystemSettingEntity entity = findEntityByNum(num);
        return entity == null ? null : toDomain(entity);
    }

    /**
     * 按分类查询。
     *
     * @param category 分类
     * @return 聚合
     */
    @Override
    public SystemSettingAggregate findByCategory(String category) {
        if (StrUtil.isBlank(category)) {
            return null;
        }
        LambdaQueryWrapper<SystemSettingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSettingEntity::getCategory, category)
                .eq(SystemSettingEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        SystemSettingEntity entity = systemSettingMapper.selectOne(wrapper);
        return entity == null ? null : toDomain(entity);
    }

    /**
     * 按业务编码软删除。
     *
     * @param num          业务编码
     * @param operatorCode 当前操作人
     */
    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SystemSettingEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemSettingEntity::getNum, num)
                .eq(SystemSettingEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SystemSettingEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SystemSettingEntity::getUpdateNo, operatorCode);
        systemSettingMapper.update(null, wrapper);
    }

    /**
     * 按业务编码查询未删除实体。
     *
     * @param num 业务编码
     * @return 实体
     */
    private SystemSettingEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        LambdaQueryWrapper<SystemSettingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSettingEntity::getNum, num)
                .eq(SystemSettingEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return systemSettingMapper.selectOne(wrapper);
    }

    /**
     * 聚合 → 实体。
     *
     * @param a 聚合
     * @return 实体
     */
    private SystemSettingEntity toEntity(SystemSettingAggregate a) {
        SystemSettingEntity e = new SystemSettingEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.category = a.getCategory();
        e.settingJson = a.getSettingJson();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    /**
     * 实体 → 聚合。
     *
     * @param e 实体
     * @return 聚合
     */
    private SystemSettingAggregate toDomain(SystemSettingEntity e) {
        SystemSettingAggregate a = new SystemSettingAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setCategory(e.category);
        a.setSettingJson(e.settingJson);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
