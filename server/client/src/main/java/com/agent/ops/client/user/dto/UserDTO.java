package com.agent.ops.client.user.dto;

import java.util.List;

/**
 * 用户基础数据传输对象。
 */
public class UserDTO {
    /**
     * 用户主键。
     */
    public Long id;

    /**
     * 用户业务编码。
     */
    public String num;

    /**
     * 用户邮箱。
     */
    public String email;

    /**
     * 用户手机号。
     */
    public String phone;

    /**
     * 用户姓名。
     */
    public String name;

    /**
     * 用户平台角色列表。
     */
    public List<UserRoleDTO> roles;

    /**
     * 用户状态。
     */
    public String status;

    /**
     * 用户备注。
     */
    public String remark;
}
