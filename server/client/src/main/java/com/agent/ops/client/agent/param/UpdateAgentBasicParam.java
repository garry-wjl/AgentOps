package com.agent.ops.client.agent.param;

import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class UpdateAgentBasicParam extends CommonRequest {
    public String num;
    public String displayName;
    public String description;
    public List<String> tags;
    public String remark;
}
