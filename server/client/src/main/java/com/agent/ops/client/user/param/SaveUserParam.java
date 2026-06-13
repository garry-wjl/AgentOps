package com.agent.ops.client.user.param;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 保存用户资料请求参数。
 */
public class SaveUserParam {
    @NotBlank(message = "用户编码不能为空")
    public String userNum;

    @NotBlank(message = "请输入邮箱")
    @Email(message = "请输入正确的邮箱格式")
    public String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    public String phone;

    @NotBlank(message = "请输入姓名")
    public String name;

    @NotEmpty(message = "请至少选择一个角色")
    public List<String> roles;

    @Size(max = 200, message = "备注不能超过 200 字")
    public String remark;
}