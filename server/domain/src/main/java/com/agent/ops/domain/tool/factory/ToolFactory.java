package com.agent.ops.domain.tool.factory;

import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.domain.tool.ToolAggregate;

import java.util.List;

public interface ToolFactory {
    ToolAggregate create(String spaceCode, String name, ToolType type, ToolSubType subType,
                         String description, List<String> tags, String configJson, String remark);
    ToolAggregate createByNum(String num);
}
