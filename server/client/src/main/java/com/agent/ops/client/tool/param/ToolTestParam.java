package com.agent.ops.client.tool.param;

import com.agent.ops.facade.request.CommonRequest;

import java.util.Map;

public class ToolTestParam extends CommonRequest {
    public String num;
    /**
     * 试运行入参（FunctionCall：path/query/header/body 实参；MCP：可空）。
     */
    public Map<String, Object> testInput;
}
