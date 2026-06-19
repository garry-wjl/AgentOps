package com.agent.ops.domain.agent.repository;

import com.agent.ops.domain.agent.AgentAggregate;

public interface AgentRepository {
    void save(AgentAggregate aggregate);
    AgentAggregate findByNum(String num);
    AgentAggregate findByName(String spaceCode, String name);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
}
