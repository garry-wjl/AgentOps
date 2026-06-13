package com.agent.ops.infra.agent.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.agent.enums.AgentVersionStatus;
import com.agent.ops.domain.agent.AgentVersionAggregate;
import com.agent.ops.domain.agent.factory.AgentVersionFactory;
import com.agent.ops.domain.agent.gateway.AgentGateway;
import com.agent.ops.domain.agent.repository.AgentVersionRepository;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AgentVersionFactoryImpl implements AgentVersionFactory {
    @Resource
    private AgentVersionRepository agentVersionRepository;

    @Resource
    private AgentGateway agentGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public AgentVersionAggregate create(String agentCode, String versionNo, AssemblySnapshot snapshot) {
        Assert.notBlank(agentCode, "agentCode 不能为空");
        AgentVersionAggregate a = new AgentVersionAggregate(agentVersionRepository, agentGateway, domainEventPublisher);
        a.setAgentCode(agentCode);
        a.setVersionNo(versionNo);
        a.setSnapshot(snapshot == null ? new AssemblySnapshot() : snapshot);
        a.setStatus(AgentVersionStatus.DRAFT);
        return a;
    }

    @Override
    public AgentVersionAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        AgentVersionAggregate a = agentVersionRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(agentVersionRepository);
        a.setGateway(agentGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
