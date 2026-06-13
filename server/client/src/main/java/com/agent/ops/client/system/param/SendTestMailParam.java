package com.agent.ops.client.system.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 测试发邮件入参。
 */
public class SendTestMailParam extends CommonRequest {
    /**
     * 收件人邮箱。
     */
    public String to;
}
