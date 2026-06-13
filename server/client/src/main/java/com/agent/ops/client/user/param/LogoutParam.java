package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;

/**
 * 退出登录请求参数。
 */
public class LogoutParam {
    @NotBlank(message = "访问令牌不能为空")
    public String token;
}