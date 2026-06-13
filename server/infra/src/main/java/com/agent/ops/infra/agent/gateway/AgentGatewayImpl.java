package com.agent.ops.infra.agent.gateway;

import com.agent.ops.domain.agent.gateway.AgentGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AgentGatewayImpl implements AgentGateway {
    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Override
    public String generateAgentCode() {
        return bizCodeGenerator.generate(BizCodePrefix.AGENT);
    }

    @Override
    public String generateVersionCode() {
        return bizCodeGenerator.generate(BizCodePrefix.AGENT_VERSION);
    }
}
