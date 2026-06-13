package com.agent.ops.domain.skill.event;

public final class SkillEventConstant {
    public static final String SKILL_CREATED = "skill.skill.created";
    public static final String SKILL_ENABLED = "skill.skill.enabled";
    public static final String SKILL_WITHDRAWN = "skill.skill.withdrawn";
    public static final String SKILL_DELETED = "skill.skill.deleted";
    public static final String SKILL_CURRENT_VERSION_REFRESHED = "skill.skill.current_version_refreshed";

    public static final String VERSION_CREATED = "skill.skill_version.created";
    public static final String VERSION_PUBLISHED = "skill.skill_version.published";
    public static final String VERSION_WITHDRAWN = "skill.skill_version.withdrawn";
    public static final String VERSION_DELETED = "skill.skill_version.deleted";

    private SkillEventConstant() { }
}
