package com.agent.ops.infra.system.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.AuditLogAggregate;
import com.agent.ops.domain.system.repository.AuditLogRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.system.entity.AuditLogEntity;
import com.agent.ops.infra.system.mapper.AuditLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 审计日志仓储实现。
 */
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {
    /**
     * 审计日志 Mapper。
     */
    @Resource
    private AuditLogMapper auditLogMapper;

    /**
     * 持久化。
     *
     * @param aggregate 聚合
     */
    @Override
    public void save(AuditLogAggregate aggregate) {
        AuditLogEntity entity = toEntity(aggregate);
        AuditLogEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            auditLogMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        auditLogMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    /**
     * 按业务编码查询。
     *
     * @param num 业务编码
     * @return 聚合
     */
    @Override
    public AuditLogAggregate findByNum(String num) {
        AuditLogEntity entity = findEntityByNum(num);
        return entity == null ? null : toDomain(entity);
    }

    /**
     * 按业务编码软删除。
     *
     * @param num          业务编码
     * @param operatorCode 操作人
     */
    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<AuditLogEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AuditLogEntity::getNum, num)
                .eq(AuditLogEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(AuditLogEntity::getIsDeleted, InfraConstant.DELETED)
                .set(AuditLogEntity::getUpdateNo, operatorCode);
        auditLogMapper.update(null, wrapper);
    }

    /**
     * 按业务编码查询未删除实体。
     *
     * @param num 业务编码
     * @return 实体
     */
    private AuditLogEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        LambdaQueryWrapper<AuditLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogEntity::getNum, num)
                .eq(AuditLogEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return auditLogMapper.selectOne(wrapper);
    }

    /**
     * 聚合 → 实体。
     *
     * @param a 聚合
     * @return 实体
     */
    private AuditLogEntity toEntity(AuditLogAggregate a) {
        AuditLogEntity e = new AuditLogEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.module = a.getModule();
        e.action = a.getAction();
        e.operatorCode = a.getOperatorCodeField();
        e.targetNum = a.getTargetNum();
        e.detailJson = a.getDetailJson();
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
    private AuditLogAggregate toDomain(AuditLogEntity e) {
        AuditLogAggregate a = new AuditLogAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setModule(e.module);
        a.setAction(e.action);
        a.setOperatorCodeField(e.operatorCode);
        a.setTargetNum(e.targetNum);
        a.setDetailJson(e.detailJson);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
