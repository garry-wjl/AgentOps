package com.agent.ops.adapter.config;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.user.entity.UserEntity;
import com.agent.ops.infra.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器，校验当前操作用户是否具备 ADMIN 平台角色。
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    /**
     * 用户表 Mapper。
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 存储操作人标识的请求属性名称。
     */
    private static final String ATTR_OPERATOR_ID = "operatorId";

    /**
     * 请求处理前校验管理员角色。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object attr = request.getAttribute(ATTR_OPERATOR_ID);
        if (!(attr instanceof Long operatorId)) {
            throw new BusinessException("ACCESS_DENIED", "无权限访问该功能");
        }
        UserEntity entity = userMapper.selectById(operatorId);
        if (entity == null || InfraConstant.NOT_DELETED != nullSafe(entity.isDeleted)
                || StrUtil.isBlank(entity.roles) || !entity.roles.contains("ADMIN")) {
            throw new BusinessException("ACCESS_DENIED", "无权限访问该功能");
        }
        return true;
    }

    /**
     * 将空整数转换为默认假值。
     *
     * @param value 待转换整数
     * @return 非空整数
     */
    private int nullSafe(Integer value) {
        return value == null ? InfraConstant.FALSE_VALUE : value;
    }
}