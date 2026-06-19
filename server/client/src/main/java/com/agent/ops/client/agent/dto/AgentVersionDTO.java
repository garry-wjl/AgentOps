package com.agent.ops.client.agent.dto;

import com.agent.ops.client.agent.enums.AgentVersionStatus;

import java.time.LocalDateTime;

public class AgentVersionDTO {
    public String num;
    public String agentCode;
    public String versionNo;
    public AssemblySnapshotDTO snapshot;
    public AgentVersionStatus status;
    public LocalDateTime onlineTime;
    public LocalDateTime offlineTime;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
