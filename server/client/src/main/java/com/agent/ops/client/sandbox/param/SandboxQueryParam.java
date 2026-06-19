package com.agent.ops.client.sandbox.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;

public class SandboxQueryParam extends CommonRequest {
    public String spaceCode;
    public String keyword;
    public String status;
    public PageQuery pageQuery;
}
