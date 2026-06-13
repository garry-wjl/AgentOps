package com.agent.ops.client.space.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 创建空间入参。
 */
public class CreateSpaceParam extends CommonRequest {
    /**
     * 空间名称，1~50 字符且全局唯一。
     */
    public String name;

    /**
     * 描述。
     */
    public String description;

    /**
     * Logo URL。
     */
    public String iconUrl;
}
