package com.agent.ops.client.space.enums;

/**
 * 空间生命周期状态。本期单态 ENABLED，预留扩展。
 */
public enum SpaceStatus {
    /**
     * 启用中。
     */
    ENABLED(1);

    /**
     * 数据库存储值。
     */
    private final int code;

    SpaceStatus(int code) {
        this.code = code;
    }

    /**
     * 返回数据库存储值。
     *
     * @return 数据库存储值
     */
    public int getCode() {
        return code;
    }

    /**
     * 根据数据库存储值解析枚举。
     *
     * @param code 数据库存储值
     * @return 对应枚举值
     */
    public static SpaceStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SpaceStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
