package com.agent.ops.client.system.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 更新平台基础信息入参。
 */
public class UpdatePlatformBasicParam extends CommonRequest {
    /**
     * 平台名称。
     */
    public String platformName;

    /**
     * Logo URL。
     */
    public String logoUrl;

    /**
     * 加密密钥（Base64）。空字符串或 mask 占位时不更新。
     */
    public String encryptionKey;
}
