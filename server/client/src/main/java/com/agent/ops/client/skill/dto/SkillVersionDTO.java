package com.agent.ops.client.skill.dto;

import com.agent.ops.client.skill.enums.SkillVersionStatus;

import java.time.LocalDateTime;

public class SkillVersionDTO {
    public String num;
    public String skillCode;
    public String versionNo;
    public String skillMdContent;
    public SkillVersionStatus status;
    public LocalDateTime publishTime;
    public LocalDateTime withdrawTime;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
