package com.agent.ops.client.user.dto;

/**
 * 用户登录结果数据传输对象。
 */
public class LoginResultDTO {
    /**
     * 访问令牌。
     */
    public String accessToken;

    /**
     * 令牌类型。
     */
    public String tokenType;

    /**
     * 令牌有效期秒数。
     */
    public Long expiresIn;

    /**
     * 当前登录用户信息。
     */
    public CurrentUserDTO user;
}
