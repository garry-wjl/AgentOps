package com.agent.ops.domain.user.valueobject;

/**
 * 用户生命周期状态。
 */
public enum UserStatus {
    /**
     * 草稿状态，允许保存资料、提交和删除，不允许登录。
     */
    DRAFT,

    /**
     * 启用状态，允许登录，可被禁用或重置密码。
     */
    ENABLED,

    /**
     * 禁用状态，不允许登录，可被启用或重置密码。
     */
    DISABLED
}
