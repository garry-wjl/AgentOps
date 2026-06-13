package com.agent.ops.client.tool.param;

import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class CreateToolParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public ToolType type;
    public ToolSubType subType;
    public String description;
    public List<String> tags;
    public String configJson;
    public String remark;
}
