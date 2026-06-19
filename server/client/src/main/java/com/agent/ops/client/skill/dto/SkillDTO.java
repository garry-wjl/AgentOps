package com.agent.ops.client.skill.dto;

import com.agent.ops.client.skill.enums.SkillStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SkillDTO {
    public String num;
    public String spaceCode;
    public String name;
    public String description;
    public String currentVersionNo;
    public SkillStatus status;
    public List<String> tags;
    public String remark;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
