package com.agent.ops.client.system.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 更新沙箱默认入参。
 */
public class UpdateSandboxDefaultParam extends CommonRequest {
    /**
     * 默认接入地址。
     */
    public String baseUrl;

    /**
     * 探活间隔秒数。
     */
    public Integer heartbeatIntervalSec;
}
