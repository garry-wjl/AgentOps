package com.agent.ops.client.system.dto;


/**
 * SMTP 邮件服务配置 DTO。
 */
public class SmtpConfigDTO {
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
     * SMTP 密码（敏感字段，返回脱敏值）。
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
