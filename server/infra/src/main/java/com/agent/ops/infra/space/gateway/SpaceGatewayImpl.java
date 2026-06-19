package com.agent.ops.infra.space.gateway;

import com.agent.ops.domain.space.gateway.SpaceGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 空间领域网关基础设施实现，仅承载本领域业务编码生成能力。
 */
@Component
public class SpaceGatewayImpl implements SpaceGateway {
    /**
     * 业务编码生成器。
     */
    @Resource
    private BizCodeGenerator bizCodeGenerator;

    /**
     * 生成空间业务编码。
     *
     * @return 空间业务编码
     */
    @Override
    public String generateSpaceCode() {
        return bizCodeGenerator.generate(BizCodePrefix.SPACE);
    }
}
