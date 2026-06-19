package com.agent.ops.client.space.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 修改空间基础信息入参。
 */
public class UpdateSpaceParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 空间名称。
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
