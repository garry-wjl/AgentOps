package com.agent.ops.client.user.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建用户请求参数。
 */
public class CreateUserParam {
    /**
     * 邮箱。
     */
    @NotBlank(message = "请输入邮箱")
    public String email;

    /**
     * 手机号。
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    public String phone;

    /**
     * 姓名。
     */
    @NotBlank(message = "请输入姓名")
    public String name;

    /**
     * 平台角色编码列表。
     */
    @NotEmpty(message = "请至少选择一个角色")
    public List<String> roles;

    /**
     * 备注。
     */
    @Size(max = 200, message = "备注不能超过 200 字")
    public String remark;
}