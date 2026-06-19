package com.agent.ops.application.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.user.dto.PageResultDTO;
import com.agent.ops.client.user.dto.RoleOptionsParamDTO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.client.user.dto.UserDetailParamDTO;
import com.agent.ops.client.user.dto.UserPageParamDTO;
import com.agent.ops.client.user.dto.UserRoleDTO;
import com.agent.ops.client.user.dto.UserRoleQueryParamDTO;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.user.entity.UserEntity;
import com.agent.ops.infra.user.mapper.UserMapper;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户读用例应用服务。
 */
@Service
public class UserQueryService {
    /**
     * 用户表 Mapper。
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 根据登录账号查询用户业务编码。
     *
     * @param account 邮箱或手机号
     * @return 用户业务编码，不存在时返回 null
     */
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
     * 根据用户业务编码查询用户信息。
     *
     * @param userNum 用户业务编码
     * @return 用户传输对象，不存在时返回 null
     */
    public UserDTO getByNum(String userNum) {
        if (StrUtil.isBlank(userNum)) {
            return null;
        }
        UserEntity entity = findByNum(userNum);
        if (entity == null) {
            return null;
        }
        return toUserDTO(entity);
    }

    /**
     * 根据用户主键查询用户信息。
     *
     * @param id 用户主键
     * @return 用户传输对象，不存在时返回 null
     */
    public UserDTO getById(Long id) {
        if (id == null) {
            return null;
        }
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return toUserDTO(entity);
    }

    /**
     * 批量根据用户业务编码查询用户信息（自动过滤已删除）。
     *
     * @param userCodes 用户业务编码列表
     * @return 用户传输对象列表，按入参顺序无保证；空入参返回空列表
     */
    public List<UserDTO> listByCodes(List<String> userCodes) {
        if (CollUtil.isEmpty(userCodes)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserEntity::getNum, userCodes)
                .eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        List<UserEntity> entities = userMapper.selectList(wrapper);
        return toUserDTOs(entities);
    }

    /**
     * 根据用户业务编码查询密码哈希。
     *
     * @param userNum 用户业务编码
     * @return 密码哈希，用户不存在时返回 null
     */
    public String getPasswordHashByNum(String userNum) {
        if (StrUtil.isBlank(userNum)) {
            return null;
        }
        UserEntity entity = findByNum(userNum);
        if (entity == null) {
            return null;
        }
        return entity.passwordHash;
    }

    /**
     * 分页查询用户列表。
     *
     * @param param 用户分页查询参数
     * @return 用户分页结果
     */
    public PageResultDTO<UserDTO> page(UserPageParamDTO param) {
        Assert.notNull(param, "用户分页查询参数不能为空");
        LambdaQueryWrapper<UserEntity> wrapper = buildPageWrapper(param);
        Long total = userMapper.selectCount(wrapper);
        Integer pageNo = normalizePageNo(param.pageNo);
        Integer pageSize = normalizePageSize(param.pageSize);
        wrapper.last("limit " + ((pageNo - 1) * pageSize) + "," + pageSize);
        List<UserEntity> entities = userMapper.selectList(wrapper);
        PageResultDTO<UserDTO> result = new PageResultDTO<>();
        result.total = total;
        result.pageNo = pageNo;
        result.pageSize = pageSize;
        result.records = toUserDTOs(entities);
        return result;
    }

    /**
     * 查询用户详情。
     *
     * @param param 用户详情查询参数
     * @return 用户基础数据传输对象
     */
    public UserDTO detail(UserDetailParamDTO param) {
        Assert.notNull(param, "用户详情查询参数不能为空");
        Assert.notBlank(param.userNum, "用户业务编码不能为空");
        UserEntity entity = findByNum(param.userNum);
        Assert.notNull(entity, "用户不存在");
        return toUserDTO(entity);
    }

    /**
     * 查询平台内置角色选项。
     *
     * @param param 平台角色选项查询参数
     * @return 平台角色数据传输对象列表
     */
    public List<UserRoleDTO> roleOptions(RoleOptionsParamDTO param) {
        Assert.notNull(param, "平台角色选项查询参数不能为空");
        List<UserRoleDTO> roles = new ArrayList<>();
        UserRoleDTO admin = new UserRoleDTO();
        admin.code = UserRole.ADMIN_CODE;
        admin.label = "管理员";
        roles.add(admin);
        UserRoleDTO member = new UserRoleDTO();
        member.code = UserRole.MEMBER_CODE;
        member.label = "普通成员";
        roles.add(member);
        return roles;
    }

    /**
     * 查询指定用户平台角色。
     *
     * @param param 指定用户角色查询参数
     * @return 平台角色数据传输对象列表
     */
    public List<UserRoleDTO> userRoles(UserRoleQueryParamDTO param) {
        Assert.notNull(param, "用户角色查询参数不能为空");
        Assert.notBlank(param.userNum, "用户业务编码不能为空");
        UserEntity entity = findByNum(param.userNum);
        Assert.notNull(entity, "用户不存在");
        return toRoleDTOs(toDomainRoles(entity.roles));
    }

    /**
     * 将领域角色列表转换为角色数据传输对象列表。
     *
     * @param roles 领域角色列表
     * @return 角色数据传输对象列表
     */
    public List<UserRoleDTO> toRoleDTOs(List<UserRole> roles) {
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        List<UserRoleDTO> roleDTOs = new ArrayList<>();
        for (UserRole role : roles) {
            UserRoleDTO roleDTO = new UserRoleDTO();
            roleDTO.code = role.getCode();
            roleDTO.label = role.getLabel();
            roleDTOs.add(roleDTO);
        }
        return roleDTOs;
    }

    /**
     * 根据分页参数构建查询条件。
     *
     * @param param 用户分页查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<UserEntity> buildPageWrapper(UserPageParamDTO param) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(query -> query.like(UserEntity::getNum, param.keyword)
                    .or().like(UserEntity::getName, param.keyword)
                    .or().like(UserEntity::getEmail, param.keyword)
                    .or().like(UserEntity::getPhone, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            wrapper.eq(UserEntity::getStatus, param.status);
        }
        if (StrUtil.isNotBlank(param.role)) {
            wrapper.like(UserEntity::getRoles, param.role);
        }
        wrapper.orderByDesc(UserEntity::getUpdateTime);
        return wrapper;
    }

    /**
     * 根据用户业务编码查询未删除用户实体。
     *
     * @param userNum 用户业务编码
     * @return 用户实体
     */
    private UserEntity findByNum(String userNum) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getNum, userNum)
                .eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return userMapper.selectOne(wrapper);
    }

    /**
     * 将用户实体列表转换为用户数据传输对象列表。
     *
     * @param entities 用户实体列表
     * @return 用户数据传输对象列表
     */
    private List<UserDTO> toUserDTOs(List<UserEntity> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<UserDTO> results = new ArrayList<>();
        for (UserEntity entity : entities) {
            results.add(toUserDTO(entity));
        }
        return results;
    }

    /**
     * 将用户实体转换为用户数据传输对象。
     *
     * @param entity 用户实体
     * @return 用户数据传输对象
     */
    private UserDTO toUserDTO(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.id = entity.id;
        dto.num = entity.num;
        dto.email = entity.email;
        dto.phone = entity.phone;
        dto.name = entity.name;
        dto.roles = toRoleDTOs(toDomainRoles(entity.roles));
        dto.status = entity.status;
        dto.remark = entity.remark;
        return dto;
    }

    /**
     * 将角色 JSON 转换为领域角色列表。
     *
     * @param rolesJson 角色 JSON
     * @return 领域角色列表
     */
    private List<UserRole> toDomainRoles(String rolesJson) {
        if (StrUtil.isBlank(rolesJson)) {
            return Collections.emptyList();
        }
        List<UserRole> roles = JSON.parseArray(rolesJson, UserRole.class);
        return CollUtil.isEmpty(roles) ? Collections.emptyList() : roles;
    }

    /**
     * 归一化页码。
     *
     * @param pageNo 页码
     * @return 归一化页码
     */
    private Integer normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 归一化每页记录数。
     *
     * @param pageSize 每页记录数
     * @return 归一化每页记录数
     */
    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 200);
    }
}