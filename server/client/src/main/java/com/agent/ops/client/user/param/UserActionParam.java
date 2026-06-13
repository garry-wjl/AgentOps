package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户操作请求参数（提交、删除、启用、禁用共用）。
 */
public class UserActionParam {
    @NotBlank(message = "用户编码不能为空")
    public String userNum;
}