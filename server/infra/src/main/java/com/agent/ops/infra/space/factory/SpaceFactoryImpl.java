package com.agent.ops.infra.space.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.domain.space.SpaceAggregate;
import com.agent.ops.domain.space.factory.SpaceFactory;
import com.agent.ops.domain.space.gateway.SpaceGateway;
import com.agent.ops.domain.space.repository.SpaceRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 空间聚合工厂基础设施实现。
 */
@Component
public class SpaceFactoryImpl implements SpaceFactory {
    /**
     * 空间聚合仓储。
     */
    @Resource
    private SpaceRepository spaceRepository;

    /**
     * 空间领域网关。
     */
    @Resource
    private SpaceGateway spaceGateway;

    /**
     * 领域事件发布器。
     */
    @Resource
    private DomainEventPublisher domainEventPublisher;

    /**
     * 根据用户填写字段创建新的空间聚合。
     *
     * @param name          空间名称
     * @param description   描述
     * @param iconUrl       Logo URL
     * @param ownerUserCode 空间所有者用户业务编码
     * @return 空间聚合（已注入领域协作依赖）
     */
    @Override
    public SpaceAggregate create(String name, String description, String iconUrl, String ownerUserCode) {
        Assert.notBlank(name, "空间名称不能为空");
        Assert.notBlank(ownerUserCode, "空间所有者业务编码不能为空");
        SpaceAggregate space = new SpaceAggregate(spaceRepository, spaceGateway, domainEventPublisher);
        space.setName(name);
        space.setDescription(description);
        space.setIconUrl(iconUrl);
        space.setOwnerUserCode(ownerUserCode);
        return space;
    }

    /**
     * 根据业务编码加载已有空间聚合。
     *
     * @param num 空间业务编码
     * @return 空间聚合，不存在返回 null
     */
    @Override
    public SpaceAggregate createByNum(String num) {
        Assert.notBlank(num, "空间业务编码不能为空");
        SpaceAggregate space = spaceRepository.findByNum(num);
        if (space == null) {
            return null;
        }
        space.setRepository(spaceRepository);
        space.setGateway(spaceGateway);
        space.setEventPublisher(domainEventPublisher);
        return space;
    }
}
