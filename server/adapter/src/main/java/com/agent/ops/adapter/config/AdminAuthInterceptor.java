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
     * 请求处理前校验管理员角色。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object attr = request.getAttribute(TokenAuthInterceptor.ATTR_CURRENT_USER_CODE);
        if (!(attr instanceof String currentUserCode) || StrUtil.isBlank(currentUserCode)) {
            throw new BusinessException("ACCESS_DENIED", "无权限访问该功能");
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getNum, currentUserCode)
                .eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        UserEntity entity = userMapper.selectOne(wrapper);
        if (entity == null || StrUtil.isBlank(entity.roles) || !entity.roles.contains("ADMIN")) {
            throw new BusinessException("ACCESS_DENIED", "无权限访问该功能");
        }
        return true;
    }
}
