package com.agent.ops.domain.agent.repository;

import com.agent.ops.domain.agent.AgentVersionAggregate;

import java.util.List;

public interface AgentVersionRepository {
    void save(AgentVersionAggregate aggregate);
    AgentVersionAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    AgentVersionAggregate findOnlineByAgentCode(String agentCode);
    AgentVersionAggregate findDraftByAgentCode(String agentCode);
    List<AgentVersionAggregate> listByAgentCode(String agentCode);
    boolean existsByVersionNo(String agentCode, String versionNo, String excludeNum);
}
