package com.agent.ops.client.space.param;

import com.agent.ops.client.space.enums.SpaceRoleType;
import com.agent.ops.facade.request.CommonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改成员角色入参，在管理员与普通成员之间迁移。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChangeMemberRoleParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 待修改角色的用户业务编码。
     */
    public String userCode;

    /**
     * 新角色。
     */
    public SpaceRoleType roleType;
}
