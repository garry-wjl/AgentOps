package com.agent.ops.client.space.enums;

/**
 * 空间成员角色类型。
 */
public enum SpaceRoleType {
    /**
     * 管理员（含 owner）。
     */
    ADMIN(1),
    /**
     * 普通成员。
     */
    MEMBER(2);

    /**
     * 数据库存储值。
     */
    private final int code;

    SpaceRoleType(int code) {
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
    public static SpaceRoleType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SpaceRoleType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
