package com.agent.ops.client.system.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 更新 SMTP 入参。
 */
public class UpdateSmtpParam extends CommonRequest {
    /**
     * SMTP 主机。
     */
    public String host;

    /**
     * SMTP 端口。
     */
    public Integer port;

    /**
     * SMTP 用户名。
     */
    public String username;

    /**
     * SMTP 密码。空字符串或 mask 占位时不更新。
     */
    public String password;

    /**
     * 发件人邮箱。
     */
    public String from;

    /**
     * 是否使用 SSL。
     */
    public Boolean ssl;
}
