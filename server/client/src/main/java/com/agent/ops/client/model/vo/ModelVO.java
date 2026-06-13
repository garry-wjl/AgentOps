package com.agent.ops.client.model.vo;

import com.agent.ops.client.model.enums.ModelStatus;

import java.time.LocalDateTime;

/**
 * 模型视图对象。
 */
public class ModelVO {
    public String num;
    public String name;
    public String modelId;
    public String baseUrl;
    public String apiKey;
    public String remark;
    public ModelStatus status;
    public LocalDateTime updateTime;
}
