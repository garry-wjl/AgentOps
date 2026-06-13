package com.agent.ops.client.skill.param;

import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class UpdateSkillBasicParam extends CommonRequest {
    public String num;
    public String description;
    public List<String> tags;
    public String remark;
}
