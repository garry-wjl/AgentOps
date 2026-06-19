package com.agent.ops.client.prompt.param;

import com.agent.ops.facade.request.CommonRequest;

public class CreatePromptParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public String key;
    public String content;
    public String remark;
}
