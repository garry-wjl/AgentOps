package com.agent.ops.domain.system.repository;

import com.agent.ops.domain.system.AuditLogAggregate;

/**
 * 审计日志仓储契约。
 */
public interface AuditLogRepository {
    /**
     * 持久化审计日志。
     *
     * @param aggregate 审计日志
     */
    void save(AuditLogAggregate aggregate);

    /**
     * 按业务编码查询。
     *
     * @param num 业务编码
     * @return 审计日志
     */
    AuditLogAggregate findByNum(String num);

    /**
     * 按业务编码软删除（极少用）。
     *
     * @param num          业务编码
     * @param operatorCode 当前操作人
     */
    void deleteByNum(String num, String operatorCode);
}
