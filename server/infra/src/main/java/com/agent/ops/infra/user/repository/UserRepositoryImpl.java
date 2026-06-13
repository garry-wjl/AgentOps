package com.agent.ops.infra.user.repository;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.user.UserAggregate;
import com.agent.ops.domain.user.repository.UserRepository;
import com.agent.ops.domain.user.valueobject.PasswordCredential;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.domain.user.valueobject.UserStatus;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.user.entity.UserEntity;
import com.agent.ops.infra.user.mapper.UserMapper;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 用户聚合仓储基础设施实现。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    /**
     * 用户表 Mapper。
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 保存用户聚合。
     *
     * @param aggregate 用户聚合
     */
    @Override
    public void save(UserAggregate aggregate) {
        UserEntity entity = toEntity(aggregate);
        UserEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            userMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        userMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    /**
     * 根据用户业务编码查询用户聚合。
     *
     * @param num 用户业务编码
     * @return 用户聚合
     */
    @Override
    public UserAggregate findByNum(String num) {
        UserEntity entity = findEntityByNum(num);
        if (entity == null) {
            return null;
        }
        return toDomain(entity);
    }

    /**
     * 根据用户业务编码删除用户聚合。
     *
     * @param num 用户业务编码
     */
    @Override
    public void deleteByNum(String num) {
        LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserEntity::getNum, num)
                .eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(UserEntity::getIsDeleted, InfraConstant.DELETED);
        userMapper.update(null, wrapper);
    }

    /**
     * 根据用户业务编码查询未删除用户实体。
     *
     * @param num 用户业务编码
     * @return 用户实体
     */
    private UserEntity findEntityByNum(String num) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getNum, num)
                .eq(UserEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return userMapper.selectOne(wrapper);
    }

    /**
     * 将领域聚合转换为持久化对象。
     *
     * @param aggregate 用户聚合
     * @return 用户持久化对象
     */
    private UserEntity toEntity(UserAggregate aggregate) {
        UserEntity entity = new UserEntity();
        entity.id = aggregate.getId();
        entity.num = aggregate.getNum();
        entity.email = aggregate.getEmail();
        entity.phone = aggregate.getPhone();
        entity.name = aggregate.getName();
        entity.roles = JSON.toJSONString(aggregate.getRoles());
        entity.status = aggregate.getStatus() == null ? null : aggregate.getStatus().name();
        entity.passwordHash = aggregate.getCredential() == null ? null : aggregate.getCredential().getPasswordHash();
        entity.passwordSet = aggregate.getCredential() != null && Boolean.TRUE.equals(aggregate.getCredential().getPasswordSet())
                ? InfraConstant.TRUE_VALUE : InfraConstant.FALSE_VALUE;
        entity.remark = aggregate.getRemark();
        entity.isDeleted = InfraConstant.NOT_DELETED;
        entity.createNo = aggregate.getCreateNo();
        entity.updateNo = aggregate.getUpdateNo();
        entity.createTime = aggregate.getCreateTime();
        entity.updateTime = aggregate.getUpdateTime();
        return entity;
    }

    /**
     * 将持久化对象转换为用户聚合。
     *
     * @param entity 用户持久化对象
     * @return 用户聚合
     */
    private UserAggregate toDomain(UserEntity entity) {
        UserAggregate aggregate = new UserAggregate();
        aggregate.setId(entity.id);
        aggregate.setNum(entity.num);
        aggregate.setEmail(entity.email);
        aggregate.setPhone(entity.phone);
        aggregate.setName(entity.name);
        aggregate.setRoles(parseRoles(entity.roles));
        aggregate.setStatus(StrUtil.isBlank(entity.status) ? null : UserStatus.valueOf(entity.status));
        PasswordCredential credential = new PasswordCredential();
        credential.setPasswordHash(entity.passwordHash);
        credential.setPasswordSet(InfraConstant.TRUE_VALUE == nullSafe(entity.passwordSet));
        aggregate.setCredential(credential);
        aggregate.setRemark(entity.remark);
        aggregate.setCreateNo(entity.createNo);
        aggregate.setUpdateNo(entity.updateNo);
        aggregate.setCreateTime(entity.createTime);
        aggregate.setUpdateTime(entity.updateTime);
        return aggregate;
    }

    /**
     * 解析用户角色 JSON。
     *
     * @param rolesJson 用户角色 JSON
     * @return 用户角色列表
     */
    private List<UserRole> parseRoles(String rolesJson) {
        if (StrUtil.isBlank(rolesJson)) {
            return Collections.emptyList();
        }
        List<UserRole> roles = JSON.parseArray(rolesJson, UserRole.class);
        return CollUtil.isEmpty(roles) ? Collections.emptyList() : roles;
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
