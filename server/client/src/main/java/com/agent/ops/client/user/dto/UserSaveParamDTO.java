package com.agent.ops.client.user.dto;

import java.util.List;

/**
 * 保存用户资料参数。
 */
public class UserSaveParamDTO {
    /**
     * 用户业务编码。
     */
    public String userNum;

    /**
     * 邮箱。
     */
    public String email;

    /**
     * 手机号。
     */
    public String phone;

    /**
     * 姓名。
     */
    public String name;

    /**
     * 平台角色编码列表。
     */
    public List<String> roles;

    /**
     * 备注。
     */
    public String remark;

    /**
     * 操作人标识。
     */
    public Long operatorId;
}
