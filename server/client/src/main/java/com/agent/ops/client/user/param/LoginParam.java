package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 */
public class LoginParam {
    @NotBlank(message = "请输入邮箱或手机号")
    public String account;

    @NotBlank(message = "请输入密码")
    public String password;
}