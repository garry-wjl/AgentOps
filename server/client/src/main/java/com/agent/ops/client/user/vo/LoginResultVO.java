package com.agent.ops.client.user.vo;

import com.agent.ops.client.user.dto.CurrentUserDTO;

/**
 * 登录结果视图对象。
 */
public class LoginResultVO {
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