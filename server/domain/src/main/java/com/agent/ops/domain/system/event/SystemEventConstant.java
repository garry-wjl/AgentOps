package com.agent.ops.domain.system.event;

/**
 * 系统设置 / 审计日志领域事件常量。
 */
public final class SystemEventConstant {
    /**
     * 系统设置创建事件。
     */
    public static final String SETTING_CREATED = "system.setting.created";

    /**
     * 系统设置变更事件（settingJson 内容变更时发，由 SystemSettingsLoader 订阅刷新缓存）。
     */
    public static final String SETTING_CHANGED = "system.setting.changed";

    /**
     * 系统设置删除事件。
     */
    public static final String SETTING_DELETED = "system.setting.deleted";

    private SystemEventConstant() {
    }
}
