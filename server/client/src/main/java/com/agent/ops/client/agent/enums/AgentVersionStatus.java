package com.agent.ops.client.agent.enums;

public enum AgentVersionStatus {
    DRAFT(0), ONLINE(1), OFFLINE(2);

    private final int code;
    AgentVersionStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static AgentVersionStatus fromCode(Integer code) {
        if (code == null) return null;
        for (AgentVersionStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
