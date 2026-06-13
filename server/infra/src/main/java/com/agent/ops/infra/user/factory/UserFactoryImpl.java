package com.agent.ops.infra.user.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.agent.ops.domain.user.UserAggregate;
import com.agent.ops.domain.user.factory.UserFactory;
import com.agent.ops.domain.user.gateway.UserGateway;
import com.agent.ops.domain.user.repository.UserRepository;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户聚合工厂基础设施实现。
 */
@Component
public class UserFactoryImpl implements UserFactory {
    /**
     * 用户聚合仓储。
     */
    @Resource
    private UserRepository userRepository;

    /**
     * 用户领域网关。
     */
    @Resource
    private UserGateway userGateway;

    /**
     * 领域事件发布器。
     */
    @Resource
    private DomainEventPublisher domainEventPublisher;

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
    @Override
    public UserAggregate create(String email, String phone, String name, List<UserRole> roles, String remark) {
        Assert.notBlank(email, "邮箱不能为空");
        Assert.notBlank(name, "姓名不能为空");
        Assert.isTrue(CollUtil.isNotEmpty(roles), "请至少选择一个角色");
        UserAggregate aggregate = new UserAggregate(userRepository, userGateway, domainEventPublisher);
        aggregate.setEmail(email);
        aggregate.setPhone(phone);
        aggregate.setName(name);
        aggregate.setRoles(roles);
        aggregate.setRemark(remark);
        return aggregate;
    }

    /**
     * 根据用户业务编码构建既有用户聚合。
     *
     * @param num 用户业务编码
     * @return 用户聚合
     */
    @Override
    public UserAggregate createByNum(String num) {
        UserAggregate aggregate = userRepository.findByNum(num);
        if (aggregate == null) {
            return null;
        }
        aggregate.setUserRepository(userRepository);
        aggregate.setUserGateway(userGateway);
        aggregate.setDomainEventPublisher(domainEventPublisher);
        return aggregate;
    }
}
