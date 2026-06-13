package com.agent.ops.application.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.agent.ops.client.user.dto.AssignUserRolesParamDTO;
import com.agent.ops.client.user.dto.ResetPasswordParamDTO;
import com.agent.ops.client.user.dto.UserActionParamDTO;
import com.agent.ops.client.user.dto.UserCreateParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.client.user.dto.UserSaveParamDTO;
import com.agent.ops.domain.user.UserAggregate;
import com.agent.ops.domain.user.factory.UserFactory;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.util.PasswordEncryptor;
import com.agent.ops.infra.common.util.TokenProvider;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户写用例应用服务。
 */
@Service
public class UserCommandService {
    /**
     * 用户聚合工厂。
     */
    @Resource
    private UserFactory userFactory;

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
     * 创建用户草稿。
     *
     * @param param 创建用户参数
     * @return 用户基础数据
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO create(UserCreateParamDTO param) {
        Assert.notNull(param, "创建用户参数不能为空");
        UserAggregate user = userFactory.create(param.email, param.phone, param.name, toRoles(param.roles), param.remark);
        user.save(param.operatorCode);
        return toUserDTO(user);
    }

    /**
     * 保存用户资料。
     *
     * @param param 保存用户资料参数
     * @return 用户基础数据
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO save(UserSaveParamDTO param) {
        Assert.notNull(param, "保存用户参数不能为空");
        UserAggregate user = loadUser(param.userNum);
        user.setEmail(param.email);
        user.setPhone(param.phone);
        user.setName(param.name);
        user.setRoles(toRoles(param.roles));
        user.setRemark(param.remark);
        user.save(param.operatorCode);
        return toUserDTO(user);
    }

    /**
     * 提交草稿用户。
     *
     * @param param 用户动作参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void submit(UserActionParamDTO param) {
        UserAggregate user = loadUser(param.userNum);
        user.submit(param.operatorCode);
    }

    /**
     * 删除草稿用户。
     *
     * @param param 用户动作参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserActionParamDTO param) {
        UserAggregate user = loadUser(param.userNum);
        user.delete(param.operatorCode);
    }

    /**
     * 启用禁用用户。
     *
     * @param param 用户动作参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void enable(UserActionParamDTO param) {
        UserAggregate user = loadUser(param.userNum);
        user.enable(param.operatorCode);
    }

    /**
     * 禁用启用用户。
     *
     * @param param 用户动作参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void disable(UserActionParamDTO param) {
        UserAggregate user = loadUser(param.userNum);
        user.disable(param.operatorCode);
    }

    /**
     * 重置用户密码。
     *
     * @param param 重置密码参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordParamDTO param) {
        Assert.notNull(param, "重置密码参数不能为空");
        passwordEncryptor.validatePasswordPolicy(param.newPassword, param.confirmPassword);
        String passwordHash = passwordEncryptor.hash(param.newPassword);
        UserAggregate user = loadUser(param.userNum);
        user.resetPassword(passwordHash, param.operatorCode);
        tokenProvider.revokeUserTokens(param.userNum);
    }

    /**
     * 分配用户平台角色。
     *
     * @param param 分配用户平台角色参数
     * @return 用户基础数据
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO assignRoles(AssignUserRolesParamDTO param) {
        Assert.notNull(param, "分配角色参数不能为空");
        UserAggregate user = loadUser(param.userNum);
        user.assignRoles(toRoles(param.roles), param.operatorCode);
        return toUserDTO(user);
    }

    /**
     * 根据用户业务编码加载用户聚合。
     *
     * @param userNum 用户业务编码
     * @return 用户聚合
     */
    private UserAggregate loadUser(String userNum) {
        Assert.notBlank(userNum, "用户业务编码不能为空");
        UserAggregate user = userFactory.createByNum(userNum);
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }

    /**
     * 将角色编码列表转换为领域角色列表。
     *
     * @param roleCodes 角色编码列表
     * @return 领域角色列表
     */
    private List<UserRole> toRoles(List<String> roleCodes) {
        Assert.isTrue(CollUtil.isNotEmpty(roleCodes), "请至少选择一个角色");
        List<UserRole> roles = new ArrayList<>();
        for (String roleCode : roleCodes) {
            UserRole role = new UserRole();
            role.setCode(roleCode);
            role.setLabel(UserRole.ADMIN_CODE.equals(roleCode) ? "管理员" : "普通成员");
            role.validate();
            roles.add(role);
        }
        return roles;
    }

    /**
     * 将用户聚合转换为用户基础数据传输对象。
     *
     * @param user 用户聚合
     * @return 用户基础数据传输对象
     */
    private UserDTO toUserDTO(UserAggregate user) {
        UserDTO dto = new UserDTO();
        dto.id = user.getId();
        dto.num = user.getNum();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.name = user.getName();
        dto.roles = userQueryService.toRoleDTOs(user.getRoles());
        dto.status = user.getStatus() == null ? null : user.getStatus().name();
        dto.remark = user.getRemark();
        return dto;
    }
}