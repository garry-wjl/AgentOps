package com.agent.ops.domain.tool.gateway;

import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;

/**
 * 工具领域网关：仅本领域 save 服务的方法。
 */
public interface ToolGateway {
    /**
     * 生成工具业务编码。
     *
     * @return 业务编码
     */
    String generateToolCode();

    /**
     * 按子类型校验配置 JSON。
     *
     * @param type    类型
     * @param subType 子类型
     * @param json    配置 JSON
     */
    void validateConfig(ToolType type, ToolSubType subType, String json);

    /**
     * 加密配置 JSON 中的敏感字段，返回新 JSON 字符串。
     *
     * @param json 原始 JSON
     * @return 已加密敏感字段的 JSON
     */
    String encryptSensitiveFields(String json);
}
