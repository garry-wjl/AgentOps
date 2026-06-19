package com.agent.ops.domain.space.gateway;

/**
 * 空间领域网关：仅承载本领域必要的业务编码生成能力（公共方案 §11.6）。
 */
public interface SpaceGateway {
    /**
     * 生成空间业务编码。
     *
     * @return 空间业务编码
     */
    String generateSpaceCode();
}
