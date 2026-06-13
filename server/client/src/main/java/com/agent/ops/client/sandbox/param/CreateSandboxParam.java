package com.agent.ops.client.sandbox.param;

import com.agent.ops.facade.request.CommonRequest;

public class CreateSandboxParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public String image;
    public String baseUrlOverride;
    public String remark;
}
