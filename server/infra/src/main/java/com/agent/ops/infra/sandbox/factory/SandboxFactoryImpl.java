package com.agent.ops.infra.sandbox.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.domain.sandbox.SandboxAggregate;
import com.agent.ops.domain.sandbox.factory.SandboxFactory;
import com.agent.ops.domain.sandbox.gateway.SandboxGateway;
import com.agent.ops.domain.sandbox.repository.SandboxRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SandboxFactoryImpl implements SandboxFactory {
    @Resource
    private SandboxRepository sandboxRepository;

    @Resource
    private SandboxGateway sandboxGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SandboxAggregate create(String spaceCode, String name, String image, String baseUrlOverride, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        SandboxAggregate a = new SandboxAggregate(sandboxRepository, sandboxGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setImage(image);
        a.setBaseUrlOverride(baseUrlOverride);
        a.setRemark(remark);
        a.setStatus(SandboxStatus.DRAFT);
        return a;
    }

    @Override
    public SandboxAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        SandboxAggregate a = sandboxRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(sandboxRepository);
        a.setGateway(sandboxGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
