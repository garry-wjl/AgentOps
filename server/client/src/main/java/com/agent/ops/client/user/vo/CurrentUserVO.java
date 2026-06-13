package com.agent.ops.client.user.vo;

import com.agent.ops.client.user.dto.UserRoleDTO;

import java.util.List;

/**
 * 当前用户视图对象。
 */
public class CurrentUserVO {
    /**
     * 用户主键。
     */
    public Long id;

    /**
     * 用户业务编码。
     */
    public String num;

    /**
     * 用户姓名。
     */
    public String name;

    /**
     * 用户邮箱。
     */
    public String email;

    /**
     * 用户手机号。
     */
    public String phone;

    /**
     * 用户平台角色列表。
     */
    public List<UserRoleDTO> roles;

    /**
     * 可访问菜单编码列表。
     */
    public List<String> menus;
}