package com.agent.ops.infra.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Component;

/**
 * 密码加密与校验基础设施工具。
 */
@Component
public class PasswordEncryptor {
    /**
     * 生成密码安全哈希。
     *
     * @param rawPassword 明文密码
     * @return 密码哈希
     */
    public String hash(String rawPassword) {
        Assert.notBlank(rawPassword, "密码不能为空");
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码与密码哈希是否匹配。
     *
     * @param rawPassword 明文密码
     * @param passwordHash 密码哈希
     * @return 匹配结果
     */
    public boolean matches(String rawPassword, String passwordHash) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(passwordHash)) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, passwordHash);
    }

    /**
     * 校验密码策略与确认密码一致性。
     *
     * @param rawPassword 明文密码
     * @param confirmPassword 确认密码
     */
    public void validatePasswordPolicy(String rawPassword, String confirmPassword) {
        Assert.notBlank(rawPassword, "请输入新密码");
        Assert.isTrue(rawPassword.equals(confirmPassword), "两次输入的密码不一致");
        Assert.isTrue(StrUtil.length(rawPassword) >= 8
                        && ReUtil.contains("[A-Za-z]", rawPassword)
                        && ReUtil.contains("\\d", rawPassword),
                "密码至少 8 位且必须包含字母和数字");
    }
}