package com.agent.ops.domain.skill.repository;

import com.agent.ops.domain.skill.SkillResourceFileAggregate;

import java.util.List;

public interface SkillResourceFileRepository {
    void save(SkillResourceFileAggregate aggregate);
    SkillResourceFileAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    List<SkillResourceFileAggregate> listByVersionCode(String versionCode);
    boolean existsByPath(String versionCode, String path, String excludeNum);
}
