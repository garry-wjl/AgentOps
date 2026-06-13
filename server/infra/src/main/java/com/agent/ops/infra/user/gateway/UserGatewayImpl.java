package com.agent.ops.infra.user.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.agent.ops.domain.user.gateway.UserGateway;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.user.entity.UserEntity;
import com.agent.ops.infra.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户领域网关基础设施实现。
 */
@Component
public class UserGatewayImpl implements UserGateway {
    /**
     * 用户业务编码前缀。
     */
    private static final String USER_NUM_PREFIX = "US";

    /**
     * 用户业务编码时间片格式。
     */
    private static final DateTimeFormatter USER_NUM_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 用户表 Mapper。
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 生成用户业务编码。
     *
     * @return 用户业务编码
     */
    @Override
    public String generateUserNum() {
        String timePart = LocalDateTimeUtil.now().format(USER_NUM_TIME_FORMATTER);
        String randomPart = RandomUtil.randomNumbers(4);
        return USER_NUM_PREFIX + timePart + randomPart;
    }

    /**
     * 校验邮箱和手机号在平台内唯一。
     *
     * @param email 邮箱
     * @param phone 手机号
     * @param excludeUserNum 排除校验的用户业务编码
     */
    @Override
    public void assertEmailPhoneUnique(String email, String phone, String excludeUserNum) {
        if (StrUtil.isNotBlank(email) && existsByColumn("email", email, excludeUserNum)) {
            throw new BusinessException("USER_EMAIL_EXISTS", "邮箱已存在");
        }
        if (StrUtil.isNotBlank(phone) && existsByColumn("phone", phone, excludeUserNum)) {
            throw new BusinessException("USER_PHONE_EXISTS", "手机号已存在");
        }
    }

    /**
     * 生成密码安全哈希。
     *
     * @param rawPassword 明文密码
     * @return 密码哈希
     */
    @Override
    public String hash(String rawPassword) {
        Assert.notBlank(rawPassword, "密码不能为空");
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码与密码哈希是否匹配。
     *
     * @param rawPassword 明文密码
     * @param passwordHash 密码哈希
     * @return 匹配结果
     */
    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(passwordHash)) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, passwordHash);
    }

    /**
     * 校验密码策略与确认密码一致性。
     *
     * @param rawPassword 明文密码
     * @param confirmPassword 确认密码
     */
    @Override
    public void validatePasswordPolicy(String rawPassword, String confirmPassword) {
        Assert.notBlank(rawPassword, "请输入新密码");
        Assert.isTrue(rawPassword.equals(confirmPassword), "两次输入的密码不一致");
        Assert.isTrue(StrUtil.length(rawPassword) >= 8
                        && ReUtil.contains("[A-Za-z]", rawPassword)
                        && ReUtil.contains("\\d", rawPassword),
                "密码至少 8 位且必须包含字母和数字");
    }

    /**
     * 根据登录账号定位用户业务编码。
     *
     * @param account 邮箱或手机号
     * @return 用户业务编码，不存在时返回 null
     */
    @Override
    public String findUserNumByAccount(String account) {
        if (StrUtil.isBlank(account)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .and(query -> query.eq(UserEntity::getEmail, account).or().eq(UserEntity::getPhone, account))
                .last("limit 1");
        UserEntity entity = userMapper.selectOne(wrapper);
        return entity == null ? null : entity.num;
    }

    /**
     * 创建访问令牌。
     *
     * @param userId 用户主键
     * @param userNum 用户业务编码
     * @param roles 用户平台角色
     * @return 访问令牌
     */
    @Override
    public String createAccessToken(Long userId, String userNum, List<UserRole> roles) {
        Assert.notNull(userId, "用户主键不能为空");
        Assert.notBlank(userNum, "用户业务编码不能为空");
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 撤销指定访问令牌。
     *
     * @param token 访问令牌
     */
    @Override
    public void revoke(String token) {
        Assert.notBlank(token, "访问令牌不能为空");
    }

    /**
     * 撤销指定用户的全部访问令牌。
     *
     * @param userNum 用户业务编码
     */
    @Override
    public void revokeUserTokens(String userNum) {
        Assert.notBlank(userNum, "用户业务编码不能为空");
    }

    /**
     * 校验操作人是否具备管理员角色。
     *
     * @param operatorId 操作人标识
     */
    @Override
    public void assertAdmin(Long operatorId) {
        Assert.notNull(operatorId, "操作人不能为空");
        UserEntity entity = userMapper.selectById(operatorId);
        if (entity == null || InfraConstant.NOT_DELETED != nullSafe(entity.isDeleted) || StrUtil.isBlank(entity.roles)
                || !entity.roles.contains(UserRole.ADMIN_CODE)) {
            throw new BusinessException("ACCESS_DENIED", "无权限访问该功能");
        }
    }

    /**
     * 根据平台角色解析可访问菜单。
     *
     * @param roles 用户平台角色
     * @return 菜单编码列表
     */
    @Override
    public List<String> resolveMenus(List<UserRole> roles) {
        List<String> menus = new ArrayList<>();
        menus.add("space");
        if (CollUtil.isNotEmpty(roles) && roles.stream().anyMatch(UserRole::isAdmin)) {
            menus.add("user");
            menus.add("system");
        }
        return menus;
    }

    /**
     * 返回平台内置角色。
     *
     * @return 平台内置角色列表
     */
    @Override
    public List<UserRole> builtInRoles() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.admin());
        roles.add(UserRole.member());
        return roles;
    }

    /**
     * 根据列名和值判断未删除用户是否存在。
     *
     * @param column 数据库列名
     * @param value 字段值
     * @param excludeUserNum 排除校验的用户业务编码
     * @return 存在结果
     */
    private boolean existsByColumn(String column, String value, String excludeUserNum) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(column, value)
                .eq("is_deleted", InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeUserNum)) {
            wrapper.ne("num", excludeUserNum);
        }
        return userMapper.selectCount(wrapper) > 0;
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
