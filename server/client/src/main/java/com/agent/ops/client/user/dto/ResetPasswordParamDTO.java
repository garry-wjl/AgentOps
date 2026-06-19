package com.agent.ops.client.user.dto;

/**
 * 重置密码参数。
 */
public class ResetPasswordParamDTO {
    /**
     * 用户业务编码。
     */
    public String userNum;

    /**
     * 新密码。
     */
    public String newPassword;

    /**
     * 确认密码。
     */
    public String confirmPassword;

    /**
     * 操作人标识。
     */
    public String operatorCode;
}
