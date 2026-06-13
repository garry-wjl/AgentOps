package com.agent.ops.client.skill.enums;

/**
 * Skill 版本生命周期状态。
 */
public enum SkillVersionStatus {
    DRAFT(0), EFFECTIVE(1), WITHDRAWN(2);

    private final int code;
    SkillVersionStatus(int code) { this.code = code; }
    public int getCode() { return code; }

    public static SkillVersionStatus fromCode(Integer code) {
        if (code == null) return null;
        for (SkillVersionStatus s : values()) if (s.code == code) return s;
        return null;
    }
}
