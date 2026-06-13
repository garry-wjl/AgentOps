package com.agent.ops.client.space.param;

import com.agent.ops.facade.request.CommonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移除空间成员入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RemoveMemberParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 待移除用户的业务编码。
     */
    public String userCode;
}
