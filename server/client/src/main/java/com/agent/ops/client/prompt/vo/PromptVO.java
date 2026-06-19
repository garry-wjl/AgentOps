package com.agent.ops.client.prompt.vo;

import com.agent.ops.client.prompt.enums.PromptStatus;

import java.time.LocalDateTime;
import java.util.List;

public class PromptVO {
    public String num;
    public String name;
    public String key;
    public String contentPreview;
    public List<String> variables;
    public String remark;
    public PromptStatus status;
    public LocalDateTime updateTime;
}
