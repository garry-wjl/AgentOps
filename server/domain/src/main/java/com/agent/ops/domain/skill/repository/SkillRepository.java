package com.agent.ops.domain.skill.repository;

import com.agent.ops.domain.skill.SkillAggregate;

public interface SkillRepository {
    void save(SkillAggregate aggregate);
    SkillAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
}
