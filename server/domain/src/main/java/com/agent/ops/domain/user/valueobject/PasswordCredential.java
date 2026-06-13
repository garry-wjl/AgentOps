package com.agent.ops.domain.user.valueobject;

import cn.hutool.core.lang.Assert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户密码凭证值对象。
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordCredential {
    /**
     * 密码安全哈希值。
     */
    private String passwordHash;

    /**
     * 是否已设置密码。
     */
    private Boolean passwordSet;

    /**
     * 创建未设置密码的凭证对象。
     *
     * @return 未设置密码的凭证对象
     */
    public static PasswordCredential empty() {
        PasswordCredential credential = new PasswordCredential();
        credential.passwordSet = Boolean.FALSE;
        return credential;
    }

    /**
     * 重置密码哈希值。
     *
     * @param passwordHash 新密码哈希值
     * @param operatorId 当前操作人标识
     */
    public void reset(String passwordHash, Long operatorId) {
        Assert.notNull(operatorId, "操作人不能为空");
        Assert.notBlank(passwordHash, "密码哈希不能为空");
        this.passwordHash = passwordHash;
        this.passwordSet = Boolean.TRUE;
    }

    /**
     * 校验用户是否已经设置密码。
     *
     * @param operatorId 当前操作人标识
     */
    public void assertPasswordSet(Long operatorId) {
        Assert.notNull(operatorId, "操作人不能为空");
        Assert.isTrue(Boolean.TRUE.equals(passwordSet), "账号未设置密码，请联系管理员");
    }



    /**
     * 返回passwordHash。
     *
     * @return passwordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 返回passwordSet。
     *
     * @return passwordSet
     */
    public Boolean getPasswordSet() {
        return passwordSet;
    }

    /**
     * 设置passwordHash。
     *
     * @param passwordHash passwordHash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 设置passwordSet。
     *
     * @param passwordSet passwordSet
     */
    public void setPasswordSet(Boolean passwordSet) {
        this.passwordSet = passwordSet;
    }
}