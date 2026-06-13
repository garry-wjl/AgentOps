package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 分配平台角色请求参数。
 */
public class AssignRolesParam {
    @NotBlank(message = "用户编码不能为空")
    public String userNum;

    @NotEmpty(message = "请至少选择一个角色")
    public List<String> roles;
}