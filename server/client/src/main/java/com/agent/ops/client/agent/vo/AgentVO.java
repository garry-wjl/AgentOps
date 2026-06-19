package com.agent.ops.client.agent.vo;

import com.agent.ops.client.agent.enums.AgentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class AgentVO {
    public String num;
    public String name;
    public String displayName;
    public String description;
    public String currentVersionNo;
    public AgentStatus status;
    public List<String> tags;
    public LocalDateTime updateTime;
}
