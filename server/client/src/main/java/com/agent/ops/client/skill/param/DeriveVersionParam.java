package com.agent.ops.client.skill.param;

import com.agent.ops.facade.request.CommonRequest;

public class DeriveVersionParam extends CommonRequest {
    public String skillCode;
    public String sourceVersionCode;
    public String newVersionNo;
}
