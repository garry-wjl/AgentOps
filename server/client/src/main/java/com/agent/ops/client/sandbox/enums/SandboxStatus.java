package com.agent.ops.client.sandbox.enums;

/**
 * 沙箱生命周期状态。
 */
public enum SandboxStatus {
    DRAFT(0), INITIALIZING(1), ONLINE(2), OFFLINE(3), DISABLED(4);

    private final int code;

    SandboxStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static SandboxStatus fromCode(Integer code) {
        if (code == null) return null;
        for (SandboxStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
