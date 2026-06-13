package com.agent.ops.infra.agent.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.agent.enums.AgentStatus;
import com.agent.ops.domain.agent.AgentAggregate;
import com.agent.ops.domain.agent.factory.AgentFactory;
import com.agent.ops.domain.agent.gateway.AgentGateway;
import com.agent.ops.domain.agent.repository.AgentRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentFactoryImpl implements AgentFactory {
    @Resource
    private AgentRepository agentRepository;

    @Resource
    private AgentGateway agentGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public AgentAggregate create(String spaceCode, String name, String displayName, String description, List<String> tags, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        AgentAggregate a = new AgentAggregate(agentRepository, agentGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setDisplayName(displayName);
        a.setDescription(description);
        a.setTags(tags);
        a.setRemark(remark);
        a.setStatus(AgentStatus.DRAFT);
        return a;
    }

    @Override
    public AgentAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        AgentAggregate a = agentRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(agentRepository);
        a.setGateway(agentGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
