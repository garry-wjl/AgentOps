package com.agent.ops.client.skill.vo;

import com.agent.ops.client.skill.enums.SkillStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SkillVO {
    public String num;
    public String name;
    public String description;
    public String currentVersionNo;
    public SkillStatus status;
    public List<String> tags;
    public LocalDateTime updateTime;
}
