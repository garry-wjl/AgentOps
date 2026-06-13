package com.agent.ops.domain.user.factory;

import com.agent.ops.domain.user.UserAggregate;
import com.agent.ops.domain.user.valueobject.UserRole;

import java.util.List;

/**
 * 用户聚合工厂接口。
 */
public interface UserFactory {
    /**
     * 根据前端创建用户时传入的基础字段构建新的用户聚合。
     *
     * @param email 邮箱
     * @param phone 手机号
     * @param name 用户姓名
     * @param roles 平台角色列表
     * @param remark 备注
     * @return 用户聚合
     */
    UserAggregate create(String email, String phone, String name, List<UserRole> roles, String remark);

    /**
     * 根据用户业务编码构建既有用户聚合。
     *
     * @param num 用户业务编码
     * @return 用户聚合
     */
    UserAggregate createByNum(String num);
}
