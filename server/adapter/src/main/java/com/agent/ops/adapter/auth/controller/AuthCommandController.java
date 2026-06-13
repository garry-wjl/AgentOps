package com.agent.ops.adapter.auth.controller;

import com.agent.ops.client.user.param.LoginParam;
import com.agent.ops.client.user.param.LogoutParam;
import com.agent.ops.client.user.vo.LoginResultVO;
import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.auth.AuthCommandService;
import com.agent.ops.client.user.dto.LoginParamDTO;
import com.agent.ops.client.user.dto.LoginResultDTO;
import com.agent.ops.client.user.dto.LogoutParamDTO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证写操作控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthCommandController extends BaseController {
    /**
     * 认证写用例应用服务。
     */
    @Resource
    private AuthCommandService authCommandService;

    /**
     * 用户登录。
     *
     * @param param 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResultVO> login(@RequestBody @Valid LoginParam param) {
        // Param → Application DTO
        LoginParamDTO dto = new LoginParamDTO();
        dto.account = param.account;
        dto.password = param.password;
        // 调用应用服务
        LoginResultDTO result = authCommandService.login(dto);
        // Application DTO → VO
        LoginResultVO vo = new LoginResultVO();
        vo.accessToken = result.accessToken;
        vo.tokenType = result.tokenType;
        vo.expiresIn = result.expiresIn;
        vo.user = result.user;
        return Result.ok(vo);
    }

    /**
     * 用户退出登录。
     *
     * @param param 退出登录参数
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestBody @Valid LogoutParam param) {
        LogoutParamDTO dto = new LogoutParamDTO();
        dto.token = param.token;
        dto.operatorId = getCurrentUserId();
        authCommandService.logout(dto);
        return Result.ok(Boolean.TRUE);
    }
}