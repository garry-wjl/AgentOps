package com.agent.ops.client.skill.param;

import com.agent.ops.facade.request.CommonRequest;

import java.util.List;

public class CreateSkillParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public String description;
    public List<String> tags;
    public String remark;
    /**
     * 创建时同步生成的 V1 草稿版本初始 SkillMD（含 frontmatter）。
     */
    public String initialSkillMd;
}
