package com.agent.ops.facade.common.mail;

/**
 * SMTP 发件客户端契约。接口位于 facade 层，infra 提供实现。
 */
public interface SmtpMailClient {
    /**
     * 按动态 SMTP 配置发送邮件。
     *
     * @param host     SMTP 主机
     * @param port     端口
     * @param username 用户名
     * @param password 密码（已解密的明文）
     * @param from     发件人
     * @param ssl      是否 SSL
     * @param to       收件人
     * @param subject  主题
     * @param body     正文
     */
    void sendMail(String host, Integer port, String username, String password,
                  String from, Boolean ssl,
                  String to, String subject, String body);
}
