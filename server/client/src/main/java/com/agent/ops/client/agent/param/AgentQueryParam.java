package com.agent.ops.client.agent.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;

public class AgentQueryParam extends CommonRequest {
    public String spaceCode;
    public String keyword;
    public String status;
    public String modelCode;
    public PageQuery pageQuery;
}
