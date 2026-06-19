package com.agent.ops.client.prompt.dto;

import com.agent.ops.client.prompt.enums.PromptStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prompt DTO。
 */
public class PromptDTO {
    public String num;
    public String spaceCode;
    public String name;
    public String key;
    public String content;
    public List<String> variables;
    public String remark;
    public PromptStatus status;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
