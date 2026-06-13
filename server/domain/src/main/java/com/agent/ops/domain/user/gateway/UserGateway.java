package com.agent.ops.domain.user.gateway;

import com.agent.ops.domain.user.valueobject.UserRole;

import java.util.List;

/**
 * 用户领域依赖的外部能力网关。
 */
public interface UserGateway {
    /**
     * 生成用户业务编码。
     *
     * @return 用户业务编码
     */
    String generateUserNum();

    /**
     * 校验邮箱和手机号在平台内唯一。
     *
     * @param email 邮箱
     * @param phone 手机号
     * @param excludeUserNum 排除校验的用户业务编码
     */
    void assertEmailPhoneUnique(String email, String phone, String excludeUserNum);

    /**
     * 生成密码安全哈希。
     *
     * @param rawPassword 明文密码
     * @return 密码哈希
     */
    String hash(String rawPassword);

    /**
     * 校验明文密码与密码哈希是否匹配。
     *
     * @param rawPassword 明文密码
     * @param passwordHash 密码哈希
     * @return 匹配结果
     */
    boolean matches(String rawPassword, String passwordHash);

    /**
     * 校验密码策略与确认密码一致性。
     *
     * @param rawPassword 明文密码
     * @param confirmPassword 确认密码
     */
    void validatePasswordPolicy(String rawPassword, String confirmPassword);

    /**
     * 根据登录账号定位用户业务编码。
     *
     * @param account 邮箱或手机号
     * @return 用户业务编码，不存在时返回 null
     */
    String findUserNumByAccount(String account);

    /**
     * 创建访问令牌。
     *
     * @param userId 用户主键
     * @param userNum 用户业务编码
     * @param roles 用户平台角色
     * @return 访问令牌
     */
    String createAccessToken(Long userId, String userNum, List<UserRole> roles);

    /**
     * 撤销指定访问令牌。
     *
     * @param token 访问令牌
     */
    void revoke(String token);

    /**
     * 撤销指定用户的全部访问令牌。
     *
     * @param userNum 用户业务编码
     */
    void revokeUserTokens(String userNum);

    /**
     * 校验操作人是否具备管理员角色。
     *
     * @param operatorId 操作人标识
     */
    void assertAdmin(Long operatorId);

    /**
     * 根据平台角色解析可访问菜单。
     *
     * @param roles 用户平台角色
     * @return 菜单编码列表
     */
    List<String> resolveMenus(List<UserRole> roles);

    /**
     * 返回平台内置角色。
     *
     * @return 平台内置角色列表
     */
    List<UserRole> builtInRoles();
}
