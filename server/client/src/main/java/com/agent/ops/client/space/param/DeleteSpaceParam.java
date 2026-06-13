package com.agent.ops.client.space.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 删除空间入参，需要确认输入空间名称。
 */
public class DeleteSpaceParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 用户输入的确认空间名称，必须与目标空间名称完全一致才允许删除。
     */
    public String confirmName;
}
