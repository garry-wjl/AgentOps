package com.agent.ops.domain.prompt.repository;

import com.agent.ops.domain.prompt.PromptAggregate;

public interface PromptRepository {
    void save(PromptAggregate aggregate);
    PromptAggregate findByNum(String num);
    PromptAggregate findByKey(String spaceCode, String key);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
    boolean existsByKey(String spaceCode, String key, String excludeNum);
}
