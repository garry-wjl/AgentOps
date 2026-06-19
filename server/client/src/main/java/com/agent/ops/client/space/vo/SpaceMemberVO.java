package com.agent.ops.client.space.vo;

import com.agent.ops.client.space.enums.SpaceRoleType;

/**
 * 空间成员视图对象。由应用层基于 Space 的 admin/member 列表 + UserQueryService 装配。
 */
public class SpaceMemberVO {
    /**
     * 用户业务编码。
     */
    public String userCode;

    /**
     * 用户姓名/展示名。
     */
    public String userName;

    /**
     * 邮箱。
     */
    public String email;

    /**
     * 手机号。
     */
    public String phone;

    /**
     * 空间内角色。
     */
    public SpaceRoleType roleType;

    /**
     * 是否为空间所有者（owner）。
     */
    public Boolean owner;
}
