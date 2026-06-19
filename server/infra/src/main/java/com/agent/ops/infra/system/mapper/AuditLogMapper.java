package com.agent.ops.infra.system.mapper;

import com.agent.ops.infra.system.entity.AuditLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志表 Mapper。
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}
