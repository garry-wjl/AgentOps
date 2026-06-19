package com.agent.ops.client.model.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 修改模型入参。
 */
public class UpdateModelParam extends CommonRequest {
    public String num;
    public String name;
    public String modelId;
    public String baseUrl;
    /**
     * API Key。空字符串或 mask 占位时不更新。
     */
    public String apiKey;
    public String remark;
}
