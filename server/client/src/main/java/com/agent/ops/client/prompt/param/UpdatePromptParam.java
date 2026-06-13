package com.agent.ops.client.prompt.param;

import com.agent.ops.facade.request.CommonRequest;

public class UpdatePromptParam extends CommonRequest {
    public String num;
    public String name;
    public String content;
    public String remark;
    /**
     * 仅草稿态可改 key。
     */
    public String newKey;
}
