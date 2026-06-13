package com.agent.ops.infra.common.constant;

/**
 * 基础设施层通用常量。
 */
public final class InfraConstant {
    /**
     * 未删除标识。
     */
    public static final int NOT_DELETED = 0;

    /**
     * 已删除标识。
     */
    public static final int DELETED = 1;

    /**
     * 布尔真值的数据库存储值。
     */
    public static final int TRUE_VALUE = 1;

    /**
     * 布尔假值的数据库存储值。
     */
    public static final int FALSE_VALUE = 0;

    /**
     * 创建不可实例化的基础设施常量类。
     */
    private InfraConstant() {
    }
}
