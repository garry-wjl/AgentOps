package com.agent.ops.adapter.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 控制器基类，提供从请求属性中获取操作人标识的能力。
 */
public class BaseController {
    /**
     * 存储操作人标识的请求属性名称。
     */
    private static final String ATTR_OPERATOR_ID = "operatorId";

    /**
     * HTTP 请求对象。
     */
    @Resource
    protected HttpServletRequest request;

    /**
     * 获取当前请求的操作人标识。
     *
     * @return 操作人标识
     */
    protected Long getCurrentUserId() {
        Object attr = request.getAttribute(ATTR_OPERATOR_ID);
        return attr instanceof Long ? (Long) attr : null;
    }

    /**
     * 判断当前请求是否已认证。
     *
     * @return 是否已认证
     */
    protected boolean isLogin() {
        return getCurrentUserId() != null;
    }
}