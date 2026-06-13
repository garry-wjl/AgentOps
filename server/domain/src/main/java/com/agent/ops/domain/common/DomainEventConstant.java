package com.agent.ops.domain.common;

/**
 * 跨领域复用的领域事件类型常量。
 */
public final class DomainEventConstant {
    /**
     * 用户创建事件。
     */
    public static final String USER_CREATED = "USER_CREATED";

    /**
     * 用户资料保存事件。
     */
    public static final String USER_SAVED = "USER_SAVED";

    /**
     * 用户删除事件。
     */
    public static final String USER_DELETED = "USER_DELETED";

    /**
     * 用户提交启用事件。
     */
    public static final String USER_SUBMITTED = "USER_SUBMITTED";

    /**
     * 用户启用事件。
     */
    public static final String USER_ENABLED = "USER_ENABLED";

    /**
     * 用户禁用事件。
     */
    public static final String USER_DISABLED = "USER_DISABLED";

    /**
     * 用户密码重置事件。
     */
    public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";

    /**
     * 用户平台角色分配事件。
     */
    public static final String USER_ROLES_ASSIGNED = "USER_ROLES_ASSIGNED";

    /**
     * 创建不可实例化的领域事件常量类。
     */
    private DomainEventConstant() {
    }
}
