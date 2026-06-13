package com.agent.ops.domain.system.factory;

import com.agent.ops.domain.system.AuditLogAggregate;

/**
 * 审计日志工厂。
 */
public interface AuditLogFactory {
    /**
     * 创建新审计日志。
     *
     * @param module       模块
     * @param action       事件
     * @param operatorCode 操作人业务编码
     * @param targetNum    目标资源业务编码
     * @param detailJson   明细 JSON
     * @return 审计日志聚合
     */
    AuditLogAggregate create(String module, String action, String operatorCode, String targetNum, String detailJson);

    /**
     * 按业务编码加载。
     *
     * @param num 业务编码
     * @return 聚合，不存在返回 null
     */
    AuditLogAggregate createByNum(String num);
}
