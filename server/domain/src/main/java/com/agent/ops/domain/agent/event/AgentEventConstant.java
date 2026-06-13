package com.agent.ops.domain.agent.event;

public final class AgentEventConstant {
    public static final String AGENT_CREATED = "agent.agent.created";
    public static final String AGENT_ENABLED = "agent.agent.enabled";
    public static final String AGENT_WITHDRAWN = "agent.agent.withdrawn";
    public static final String AGENT_DELETED = "agent.agent.deleted";
    public static final String AGENT_CURRENT_VERSION_REFRESHED = "agent.agent.current_version_refreshed";

    public static final String VERSION_CREATED = "agent.agent_version.created";
    public static final String VERSION_PUBLISHED = "agent.agent_version.published";
    public static final String VERSION_OFFLINED = "agent.agent_version.offlined";
    public static final String VERSION_DELETED = "agent.agent_version.deleted";

    private AgentEventConstant() { }
}
