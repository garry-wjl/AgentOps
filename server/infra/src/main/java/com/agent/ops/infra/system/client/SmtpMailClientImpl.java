package com.agent.ops.infra.system.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.facade.common.mail.SmtpMailClient;
import com.agent.ops.facade.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SMTP 发件客户端实现（轻量占位实现）。
 * <p>
 * 本期不引入 spring-boot-starter-mail / jakarta-mail 完整依赖，
 * 仅做参数校验与日志输出；后续接入完整 SMTP 时替换为 JavaMailSender 即可。
 */
@Component
public class SmtpMailClientImpl implements SmtpMailClient {
    private static final Logger log = LoggerFactory.getLogger(SmtpMailClientImpl.class);

    @Override
    public void sendMail(String host, Integer port, String username, String password,
                         String from, Boolean ssl,
                         String to, String subject, String body) {
        Assert.notBlank(host, "SMTP host 不能为空");
        Assert.notNull(port, "SMTP port 不能为空");
        Assert.notBlank(from, "发件人不能为空");
        Assert.notBlank(to, "收件人不能为空");
        if (StrUtil.isBlank(host) || StrUtil.isBlank(from)) {
            throw new BusinessException("SMTP_NOT_CONFIGURED", "SMTP 配置不完整");
        }
        log.info("[SmtpMailClient] (placeholder) host={}:{} ssl={} from={} to={} subject={} bodyLen={}",
                host, port, Boolean.TRUE.equals(ssl), from, to, subject, body == null ? 0 : body.length());
        // 真实接入：按 application.yml 注入 JavaMailSender，按入参动态构建 MimeMessage 后 send。
    }
}
