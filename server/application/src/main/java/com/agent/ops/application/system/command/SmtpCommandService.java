package com.agent.ops.application.system.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.facade.common.mail.SmtpMailClient;
import com.agent.ops.client.system.dto.SmtpConfigDTO;
import com.agent.ops.client.system.enums.SystemSettingCategory;
import com.agent.ops.client.system.param.SendTestMailParam;
import com.agent.ops.client.system.param.UpdateSmtpParam;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.factory.SystemSettingFactory;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SMTP 写应用服务。
 */
@Service
public class SmtpCommandService {
    /**
     * 工厂。
     */
    @Resource
    private SystemSettingFactory systemSettingFactory;

    /**
     * 加密器。
     */
    @Resource
    private SecretEncryptor secretEncryptor;

    /**
     * SMTP 客户端。
     */
    @Resource
    private SmtpMailClient smtpMailClient;

    /**
     * Redis 锁。
     */
    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 更新 SMTP 配置。
     *
     * @param param 参数
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SmtpConfigDTO update(UpdateSmtpParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("system:smtp", () -> {
            String category = SystemSettingCategory.SMTP.getCode();
            SystemSettingAggregate setting = systemSettingFactory.createByCategory(category);
            JSONObject json = setting == null ? new JSONObject() : JSONObject.parseObject(setting.getSettingJson());
            if (StrUtil.isNotBlank(param.host)) {
                json.put("host", param.host);
            }
            if (param.port != null) {
                json.put("port", param.port);
            }
            if (param.username != null) {
                json.put("username", param.username);
            }
            // 密码：mask 占位或空字符串时不更新；其他情况加密存储
            if (StrUtil.isNotBlank(param.password) && !secretEncryptor.isEncrypted(param.password)
                    && !param.password.contains("****")) {
                json.put("passwordCipher", secretEncryptor.encrypt(param.password));
            }
            if (param.from != null) {
                json.put("from", param.from);
            }
            if (param.ssl != null) {
                json.put("ssl", param.ssl);
            }
            String mergedJson = json.toJSONString();
            if (setting == null) {
                setting = systemSettingFactory.create(category, mergedJson);
            } else {
                setting.setSettingJson(mergedJson);
            }
            setting.save(param.getOperatorCode());
            return toDTO(json);
        });
    }

    /**
     * 发送测试邮件。
     *
     * @param param 参数
     */
    public void sendTestMail(SendTestMailParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.to, "收件人不能为空");
        SystemSettingAggregate setting = systemSettingFactory.createByCategory(SystemSettingCategory.SMTP.getCode());
        if (setting == null) {
            throw new BusinessException("SMTP_NOT_CONFIGURED", "SMTP 尚未配置");
        }
        JSONObject json = JSONObject.parseObject(setting.getSettingJson());
        String passwordCipher = json.getString("passwordCipher");
        String password = StrUtil.isBlank(passwordCipher) ? null : secretEncryptor.decrypt(passwordCipher);
        smtpMailClient.sendMail(
                json.getString("host"),
                json.getInteger("port"),
                json.getString("username"),
                password,
                json.getString("from"),
                json.getBoolean("ssl"),
                param.to,
                "AgentOps 测试邮件",
                "这是一封来自 AgentOps 的测试邮件。"
        );
    }

    /**
     * JSON → DTO（脱敏密码）。
     *
     * @param json JSON
     * @return DTO
     */
    private SmtpConfigDTO toDTO(JSONObject json) {
        SmtpConfigDTO dto = new SmtpConfigDTO();
        dto.host = json.getString("host");
        dto.port = json.getInteger("port");
        dto.username = json.getString("username");
        String passwordCipher = json.getString("passwordCipher");
        dto.password = StrUtil.isBlank(passwordCipher) ? null : secretEncryptor.mask(passwordCipher);
        dto.from = json.getString("from");
        dto.ssl = json.getBoolean("ssl");
        return dto;
    }
}
