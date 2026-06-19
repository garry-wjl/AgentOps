package com.agent.ops.domain.skill.factory;

import com.agent.ops.domain.skill.SkillAggregate;

import java.util.List;

public interface SkillFactory {
    SkillAggregate create(String spaceCode, String name, String description, List<String> tags, String remark);
    SkillAggregate createByNum(String num);
}
