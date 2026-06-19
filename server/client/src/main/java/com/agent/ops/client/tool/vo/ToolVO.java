package com.agent.ops.client.tool.vo;

import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;

import java.time.LocalDateTime;
import java.util.List;

public class ToolVO {
    public String num;
    public String name;
    public ToolType type;
    public ToolSubType subType;
    public String description;
    public List<String> tags;
    public ToolStatus status;
    public String remark;
    public LocalDateTime updateTime;
}
