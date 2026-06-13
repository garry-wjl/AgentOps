package com.agent.ops.client.tool.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;

public class ToolQueryParam extends CommonRequest {
    public String spaceCode;
    public String keyword;
    public String type;
    public String subType;
    public String status;
    public PageQuery pageQuery;
}
