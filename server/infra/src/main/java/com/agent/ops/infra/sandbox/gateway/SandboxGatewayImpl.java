package com.agent.ops.infra.sandbox.gateway;

import com.agent.ops.domain.sandbox.gateway.SandboxGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SandboxGatewayImpl implements SandboxGateway {
    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Override
    public String generateSandboxCode() {
        return bizCodeGenerator.generate(BizCodePrefix.SANDBOX);
    }
}
