package com.agent.ops.client.model.enums;

/**
 * 模型生命周期状态。
 */
public enum ModelStatus {
    /**
     * 草稿。
     */
    DRAFT(0),
    /**
     * 启用。
     */
    ENABLED(1),
    /**
     * 禁用。
     */
    DISABLED(2);

    private final int code;

    ModelStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ModelStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ModelStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
