package com.agent.ops.domain.user.repository;

import com.agent.ops.domain.user.UserAggregate;

/**
 * 用户聚合仓储接口。
 */
public interface UserRepository {
    /**
     * 保存用户聚合。
     *
     * @param aggregate 用户聚合
     */
    void save(UserAggregate aggregate);

    /**
     * 根据用户业务编码查询用户聚合。
     *
     * @param num 用户业务编码
     * @return 用户聚合
     */
    UserAggregate findByNum(String num);

    /**
     * 根据用户业务编码删除用户聚合。
     *
     * @param num 用户业务编码
     */
    void deleteByNum(String num);
}
