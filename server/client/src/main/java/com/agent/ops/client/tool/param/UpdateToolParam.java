package com.agent.ops.client.tool.param;

import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class UpdateToolParam extends CommonRequest {
    public String num;
    public String name;
    public String description;
    public List<String> tags;
    public String configJson;
    public String remark;
}
