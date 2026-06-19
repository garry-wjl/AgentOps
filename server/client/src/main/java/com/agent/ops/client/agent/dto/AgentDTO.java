package com.agent.ops.client.agent.dto;

import com.agent.ops.client.agent.enums.AgentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class AgentDTO {
    public String num;
    public String spaceCode;
    public String name;
    public String displayName;
    public String description;
    public String currentVersionNo;
    public AgentStatus status;
    public List<String> tags;
    public String remark;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
