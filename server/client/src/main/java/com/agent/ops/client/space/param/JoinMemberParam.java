package com.agent.ops.client.space.param;

import com.agent.ops.client.space.enums.SpaceRoleType;
import com.agent.ops.facade.request.CommonRequest;

/**
 * 加入空间成员入参。
 */
public class JoinMemberParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 待加入用户的业务编码。
     */
    public String userCode;

    /**
     * 加入角色：管理员或普通成员。
     */
    public SpaceRoleType roleType;
}
