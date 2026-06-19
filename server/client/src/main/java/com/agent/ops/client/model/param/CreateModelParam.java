package com.agent.ops.client.model.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 创建模型入参。
 */
public class CreateModelParam extends CommonRequest {
    public String spaceCode;
    public String name;
    public String modelId;
    public String baseUrl;
    public String apiKey;
    public String remark;
}
