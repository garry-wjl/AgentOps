package com.agent.ops.facade.common.probe;

/**
 * 沙箱探活客户端契约。
 */
public interface SandboxProbeClient {
    /**
     * 对指定 baseUrl 发起健康检查（HTTP GET <baseUrl>/health）。
     *
     * @param baseUrl 沙箱接入地址
     * @return 是否在线
     */
    boolean probe(String baseUrl);
}
