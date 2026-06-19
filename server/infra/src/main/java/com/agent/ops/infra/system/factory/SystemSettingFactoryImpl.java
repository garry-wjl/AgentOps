package com.agent.ops.infra.system.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.factory.SystemSettingFactory;
import com.agent.ops.domain.system.gateway.SystemSettingGateway;
import com.agent.ops.domain.system.repository.SystemSettingRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 系统设置工厂实现。
 */
@Component
public class SystemSettingFactoryImpl implements SystemSettingFactory {
    /**
     * 仓储。
     */
    @Resource
    private SystemSettingRepository systemSettingRepository;

    /**
     * 网关。
     */
    @Resource
    private SystemSettingGateway systemSettingGateway;

    /**
     * 事件发布器。
     */
    @Resource
    private DomainEventPublisher domainEventPublisher;

    /**
     * 创建新聚合。
     *
     * @param category    分类
     * @param settingJson JSON
     * @return 聚合
     */
    @Override
    public SystemSettingAggregate create(String category, String settingJson) {
        Assert.notBlank(category, "category 不能为空");
        Assert.notBlank(settingJson, "settingJson 不能为空");
        SystemSettingAggregate a = new SystemSettingAggregate(systemSettingRepository, systemSettingGateway, domainEventPublisher);
        a.setCategory(category);
        a.setSettingJson(settingJson);
        return a;
    }

    /**
     * 按业务编码加载。
     *
     * @param num 业务编码
     * @return 聚合
     */
    @Override
    public SystemSettingAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        SystemSettingAggregate a = systemSettingRepository.findByNum(num);
        if (a == null) {
            return null;
        }
        a.setRepository(systemSettingRepository);
        a.setGateway(systemSettingGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }

    /**
     * 按分类加载。
     *
     * @param category 分类
     * @return 聚合
     */
    @Override
    public SystemSettingAggregate createByCategory(String category) {
        Assert.notBlank(category, "category 不能为空");
        SystemSettingAggregate a = systemSettingRepository.findByCategory(category);
        if (a == null) {
            return null;
        }
        a.setRepository(systemSettingRepository);
        a.setGateway(systemSettingGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
