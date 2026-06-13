package com.agent.ops.domain.agent.factory;

import com.agent.ops.domain.agent.AgentVersionAggregate;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;

public interface AgentVersionFactory {
    AgentVersionAggregate create(String agentCode, String versionNo, AssemblySnapshot snapshot);
    AgentVersionAggregate createByNum(String num);
}
