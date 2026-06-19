package com.agent.ops.client.agent.enums;

public enum AgentStatus {
    DRAFT(0), EFFECTIVE(1), WITHDRAWN(2);

    private final int code;
    AgentStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static AgentStatus fromCode(Integer code) {
        if (code == null) return null;
        for (AgentStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
