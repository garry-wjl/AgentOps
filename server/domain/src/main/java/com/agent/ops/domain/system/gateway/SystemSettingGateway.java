package com.agent.ops.domain.system.gateway;

/**
 * 系统设置领域网关：仅承载本领域业务编码生成。
 */
public interface SystemSettingGateway {
    /**
     * 生成系统设置业务编码。
     *
     * @return 业务编码
     */
    String generateSettingCode();

    /**
     * 生成审计日志业务编码。
     *
     * @return 业务编码
     */
    String generateAuditLogCode();
}
