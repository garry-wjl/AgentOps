package com.agent.ops.client.prompt.enums;

/**
 * Prompt 生命周期状态。
 */
public enum PromptStatus {
    DRAFT(0), ENABLED(1), DISABLED(2);

    private final int code;

    PromptStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static PromptStatus fromCode(Integer code) {
        if (code == null) return null;
        for (PromptStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
