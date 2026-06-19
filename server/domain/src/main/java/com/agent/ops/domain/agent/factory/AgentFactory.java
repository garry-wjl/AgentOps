package com.agent.ops.domain.agent.factory;

import com.agent.ops.domain.agent.AgentAggregate;

import java.util.List;

public interface AgentFactory {
    AgentAggregate create(String spaceCode, String name, String displayName, String description, List<String> tags, String remark);
    AgentAggregate createByNum(String num);
}
