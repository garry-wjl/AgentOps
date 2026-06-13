package com.agent.ops.client.agent.param;

import com.agent.ops.facade.request.CommonRequest;

public class DeriveAgentVersionParam extends CommonRequest {
    public String agentCode;
    public String sourceVersionCode;
    public String newVersionNo;
}
