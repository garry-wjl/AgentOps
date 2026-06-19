package com.agent.ops.adapter.config;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 控制器基类，提供从请求属性中获取当前用户业务编码的能力。
 */
public class BaseController {
    /**
     * HTTP 请求对象。
     */
    @Resource
    protected HttpServletRequest request;

    /**
     * 获取当前请求的用户业务编码（user.num）。
     *
     * @return 当前用户业务编码，未登录时返回 null
     */
    protected String getCurrentUserCode() {
        Object attr = request.getAttribute(TokenAuthInterceptor.ATTR_CURRENT_USER_CODE);
        return attr instanceof String value && StrUtil.isNotBlank(value) ? value : null;
    }

    /**
     * 判断当前请求是否已认证。
     *
     * @return 是否已认证
     */
    protected boolean isLogin() {
        return getCurrentUserCode() != null;
    }
}
