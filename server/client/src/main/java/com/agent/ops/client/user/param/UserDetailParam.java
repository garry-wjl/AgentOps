package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户详情查询请求参数。
 */
public class UserDetailParam {
    @NotBlank(message = "用户编码不能为空")
    public String userNum;
}