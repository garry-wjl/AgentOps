package com.agent.ops.domain.agent.gateway;

/**
 * Agent 领域网关：仅本领域业务编码生成（公共方案 §11.6）。跨领域校验在应用层 AssemblyValidator。
 */
public interface AgentGateway {
    String generateAgentCode();
    String generateVersionCode();
}
