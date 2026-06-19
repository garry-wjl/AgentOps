package com.agent.ops.client.tool.enums;

/**
 * 工具生命周期状态。
 */
public enum ToolStatus {
    DRAFT(0), EFFECTIVE(1), WITHDRAWN(2);

    private final int code;

    ToolStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static ToolStatus fromCode(Integer code) {
        if (code == null) return null;
        for (ToolStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
