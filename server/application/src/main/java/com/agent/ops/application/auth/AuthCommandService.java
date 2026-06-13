package com.agent.ops.application.auth;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.user.UserQueryService;
import com.agent.ops.client.user.dto.LoginParamDTO;
import com.agent.ops.client.user.dto.LoginResultDTO;
import com.agent.ops.client.user.dto.LogoutParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.util.PasswordEncryptor;
import com.agent.ops.infra.common.util.TokenProvider;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 认证写用例应用服务。
 */
@Service
public class AuthCommandService {
    /**
     * 用户查询应用服务。
     */
    @Resource
    private UserQueryService userQueryService;

    /**
     * 密码加密工具。
     */
    @Resource
    private PasswordEncryptor passwordEncryptor;

    /**
     * 令牌工具。
     */
    @Resource
    private TokenProvider tokenProvider;

    /**
     * 登录认证并签发访问令牌。
     *
     * @param param 登录参数
     * @return 登录结果
     */
    public LoginResultDTO login(LoginParamDTO param) {
        Assert.notNull(param, "登录参数不能为空");
        Assert.notBlank(param.account, "请输入邮箱或手机号");
        Assert.notBlank(param.password, "请输入密码");
        String userNum = userQueryService.findUserNumByAccount(param.account);
        if (StrUtil.isBlank(userNum)) {
            throw new BusinessException("ACCOUNT_OR_PASSWORD_ERROR", "账号或密码错误");
        }
        UserDTO user = userQueryService.getByNum(userNum);
        if (user == null) {
            throw new BusinessException("ACCOUNT_OR_PASSWORD_ERROR", "账号或密码错误");
        }
        if (!"ENABLED".equals(user.status)) {
            throw new BusinessException("USER_NOT_ENABLED", "账号未启用或已禁用，请联系管理员");
        }
        String passwordHash = userQueryService.getPasswordHashByNum(userNum);
        if (StrUtil.isBlank(passwordHash)) {
            throw new BusinessException("USER_PASSWORD_NOT_SET", "账号未设置密码，请联系管理员");
        }
        if (!passwordEncryptor.matches(param.password, passwordHash)) {
            throw new BusinessException("ACCOUNT_OR_PASSWORD_ERROR", "账号或密码错误");
        }
        LoginResultDTO result = new LoginResultDTO();
        result.accessToken = tokenProvider.createAccessToken(user.id, user.num);
        result.tokenType = "Bearer";
        result.expiresIn = 7200L;
        return result;
    }

    /**
     * 退出登录并撤销访问令牌。
     *
     * @param param 退出登录参数
     */
    public void logout(LogoutParamDTO param) {
        Assert.notNull(param, "退出登录参数不能为空");
        Assert.notNull(param.operatorId, "操作人不能为空");
        Assert.notBlank(param.token, "访问令牌不能为空");
        tokenProvider.revoke(param.token);
    }
}