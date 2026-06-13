package com.agent.ops.client.model.dto;

import com.agent.ops.client.model.enums.ModelStatus;

import java.time.LocalDateTime;

/**
 * 模型 DTO。
 */
public class ModelDTO {
    public String num;
    public String spaceCode;
    public String name;
    public String modelId;
    public String baseUrl;
    /**
     * API Key（脱敏后展示）。
     */
    public String apiKey;
    public String remark;
    public ModelStatus status;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
