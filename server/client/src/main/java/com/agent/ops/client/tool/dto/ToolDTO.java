package com.agent.ops.client.tool.dto;

import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;

import java.time.LocalDateTime;
import java.util.List;

public class ToolDTO {
    public String num;
    public String spaceCode;
    public String name;
    public ToolType type;
    public ToolSubType subType;
    public String description;
    public List<String> tags;
    /**
     * 配置 JSON（敏感字段脱敏）。
     */
    public String configJson;
    public ToolStatus status;
    public String remark;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
