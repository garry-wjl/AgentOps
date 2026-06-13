package com.agent.ops.infra.prompt.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.prompt.enums.PromptStatus;
import com.agent.ops.domain.prompt.PromptAggregate;
import com.agent.ops.domain.prompt.factory.PromptFactory;
import com.agent.ops.domain.prompt.gateway.PromptGateway;
import com.agent.ops.domain.prompt.repository.PromptRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class PromptFactoryImpl implements PromptFactory {
    @Resource
    private PromptRepository promptRepository;

    @Resource
    private PromptGateway promptGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public PromptAggregate create(String spaceCode, String name, String key, String content, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        PromptAggregate a = new PromptAggregate(promptRepository, promptGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setKey(key);
        a.setContent(content);
        a.setRemark(remark);
        a.setStatus(PromptStatus.DRAFT);
        return a;
    }

    @Override
    public PromptAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        PromptAggregate a = promptRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(promptRepository);
        a.setGateway(promptGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
