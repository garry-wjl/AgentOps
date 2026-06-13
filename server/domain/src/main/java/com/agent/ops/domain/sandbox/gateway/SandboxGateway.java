package com.agent.ops.domain.sandbox.gateway;

/**
 * 沙箱领域网关：仅本领域业务编码生成（公共方案 §11.6）。
 */
public interface SandboxGateway {
    /**
     * 生成沙箱业务编码。
     *
     * @return 业务编码
     */
    String generateSandboxCode();
}
