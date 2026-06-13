package com.agent.ops.adapter.config;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.infra.common.util.TokenProvider;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 访问令牌解析拦截器，从 Authorization 头解析 Bearer token，将 operatorId 写入请求属性。
 */
@Component
public class TokenAuthInterceptor implements HandlerInterceptor {
    /**
     * 令牌工具。
     */
    @Resource
    private TokenProvider tokenProvider;

    /**
     * 存储操作人标识的请求属性名称。
     */
    private static final String ATTR_OPERATOR_ID = "operatorId";

    /**
     * Authorization 头前缀。
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 请求处理前解析 token。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 始终放行，是否需要登录由后续逻辑判断
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (StrUtil.isBlank(header) || !header.startsWith(BEARER_PREFIX)) {
            return true;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        Long userId = tokenProvider.resolveUserId(token);
        if (userId != null) {
            request.setAttribute(ATTR_OPERATOR_ID, userId);
        }
        return true;
    }
}
