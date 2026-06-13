package com.agent.ops.client.user.dto;

/**
 * 用户登录参数。
 */
public class LoginParamDTO {
    /**
     * 邮箱或手机号登录账号。
     */
    public String account;

    /**
     * 明文登录密码。
     */
    public String password;
}
