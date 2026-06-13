package com.agent.ops.client.system.enums;

/**
 * 系统设置分类枚举。
 */
public enum SystemSettingCategory {
    /**
     * 平台基本信息（含加密密钥）。
     */
    PLATFORM_BASIC("platform_basic"),
    /**
     * SMTP 邮件服务。
     */
    SMTP("smtp"),
    /**
     * 空间策略。
     */
    SPACE_POLICY("space_policy"),
    /**
     * 沙箱默认接入地址。
     */
    SANDBOX_DEFAULT("sandbox_default");

    /**
     * 数据库存储值。
     */
    private final String code;

    SystemSettingCategory(String code) {
        this.code = code;
    }

    /**
     * 返回数据库存储值。
     *
     * @return 数据库存储值
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据 code 解析。
     *
     * @param code 数据库存储值
     * @return 枚举值
     */
    public static SystemSettingCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SystemSettingCategory c : values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }
        return null;
    }
}
