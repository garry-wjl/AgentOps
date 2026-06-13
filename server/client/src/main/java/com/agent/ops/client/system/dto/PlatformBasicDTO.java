package com.agent.ops.client.system.dto;


/**
 * 平台基本信息 DTO。
 */
public class PlatformBasicDTO {
    /**
     * 平台名称。
     */
    public String platformName;

    /**
     * Logo URL。
     */
    public String logoUrl;

    /**
     * 加密密钥（Base64 编码 32 字节，敏感字段，仅返回脱敏值）。
     */
    public String encryptionKey;
}
