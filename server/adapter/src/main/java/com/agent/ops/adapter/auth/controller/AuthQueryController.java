package com.agent.ops.adapter.auth.controller;

import com.agent.ops.client.user.vo.CurrentUserVO;
import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.auth.AuthQueryService;
import com.agent.ops.client.user.dto.CurrentUserDTO;
import com.agent.ops.client.user.dto.CurrentUserParamDTO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证读操作控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthQueryController extends BaseController {
    /**
     * 认证读用例应用服务。
     */
    @Resource
    private AuthQueryService authQueryService;

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public Result<CurrentUserVO> current() {
        CurrentUserParamDTO param = new CurrentUserParamDTO();
        param.operatorId = getCurrentUserId();
        // 调用应用服务
        CurrentUserDTO dto = authQueryService.current(param);
        // Application DTO → VO
        CurrentUserVO vo = new CurrentUserVO();
        vo.id = dto.id;
        vo.num = dto.num;
        vo.name = dto.name;
        vo.email = dto.email;
        vo.phone = dto.phone;
        vo.roles = dto.roles;
        vo.menus = dto.menus;
        return Result.ok(vo);
    }
}