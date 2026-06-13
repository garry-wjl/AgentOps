package com.agent.ops.client.skill.enums;

/**
 * Skill 主体生命周期状态。
 */
public enum SkillStatus {
    DRAFT(0), EFFECTIVE(1), WITHDRAWN(2);

    private final int code;
    SkillStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static SkillStatus fromCode(Integer code) {
        if (code == null) return null;
        for (SkillStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
