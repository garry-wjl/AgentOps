package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求参数。
 */
public class ResetPasswordParam {
    @NotBlank(message = "用户编码不能为空")
    public String userNum;

    @NotBlank(message = "请输入新密码")
    @Size(min = 8, message = "密码至少 8 位")
    public String newPassword;

    @NotBlank(message = "请确认新密码")
    public String confirmPassword;
}