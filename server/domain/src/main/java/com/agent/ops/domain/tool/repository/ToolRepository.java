package com.agent.ops.domain.tool.repository;

import com.agent.ops.domain.tool.ToolAggregate;

public interface ToolRepository {
    void save(ToolAggregate aggregate);
    ToolAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
}
