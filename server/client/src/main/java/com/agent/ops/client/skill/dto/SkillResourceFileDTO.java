package com.agent.ops.client.skill.dto;

import com.agent.ops.client.skill.enums.FileType;

import java.time.LocalDateTime;

public class SkillResourceFileDTO {
    public String num;
    public String skillVersionCode;
    public String path;
    public FileType type;
    public String content;
    public Long sizeBytes;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
