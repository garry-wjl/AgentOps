package com.agent.ops.application.auth;

import cn.hutool.core.lang.Assert;
import com.agent.ops.application.user.UserQueryService;
import com.agent.ops.client.user.dto.CurrentUserDTO;
import com.agent.ops.client.user.dto.CurrentUserParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 认证读用例应用服务。
 */
@Service
public class AuthQueryService {
    /**
     * 用户查询应用服务。
     */
    @Resource
    private UserQueryService userQueryService;

    /**
     * 查询当前登录用户信息。
     *
     * @param param 当前用户查询参数
     * @return 当前登录用户数据传输对象
     */
    public CurrentUserDTO current(CurrentUserParamDTO param) {
        Assert.notNull(param, "当前用户查询参数不能为空");
        Assert.notNull(param.operatorId, "操作人不能为空");
        UserDTO user = userQueryService.getById(param.operatorId);
        if (user == null) {
            return null;
        }
        CurrentUserDTO dto = new CurrentUserDTO();
        dto.id = user.id;
        dto.num = user.num;
        dto.name = user.name;
        dto.email = user.email;
        dto.phone = user.phone;
        dto.roles = user.roles;
        return dto;
    }
}