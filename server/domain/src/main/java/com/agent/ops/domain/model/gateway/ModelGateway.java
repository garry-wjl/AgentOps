package com.agent.ops.domain.model.gateway;

/**
 * 模型领域网关：仅本领域职能。
 */
public interface ModelGateway {
    /**
     * 生成模型业务编码。
     *
     * @return 业务编码
     */
    String generateModelCode();

    /**
     * 加密 API Key。
     *
     * @param plaintext 明文
     * @return 密文
     */
    String encrypt(String plaintext);
}
