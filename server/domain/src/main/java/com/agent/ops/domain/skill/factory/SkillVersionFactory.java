package com.agent.ops.domain.skill.factory;

import com.agent.ops.domain.skill.SkillVersionAggregate;

public interface SkillVersionFactory {
    SkillVersionAggregate create(String skillCode, String versionNo, String skillMdContent);
    SkillVersionAggregate createByNum(String num);
}
