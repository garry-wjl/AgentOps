package com.agent.ops.domain.skill.repository;

import com.agent.ops.domain.skill.SkillVersionAggregate;

import java.util.List;

public interface SkillVersionRepository {
    void save(SkillVersionAggregate aggregate);
    SkillVersionAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    /**
     * 找当前 Skill 的生效版本（如有）。
     *
     * @param skillCode Skill 业务编码
     * @return 生效版本聚合，没有则 null
     */
    SkillVersionAggregate findEffectiveBySkillCode(String skillCode);

    /**
     * 找当前 Skill 的草稿版本（如有）。
     *
     * @param skillCode Skill 业务编码
     * @return 草稿版本聚合，没有则 null
     */
    SkillVersionAggregate findDraftBySkillCode(String skillCode);

    /**
     * 列出指定 Skill 的全部未删除版本。
     *
     * @param skillCode Skill 业务编码
     * @return 版本列表
     */
    List<SkillVersionAggregate> listBySkillCode(String skillCode);

    /**
     * 校验版本号在 Skill 内唯一。
     *
     * @param skillCode  Skill 业务编码
     * @param versionNo  版本号
     * @param excludeNum 排除自检的版本业务编码
     * @return 是否已存在
     */
    boolean existsByVersionNo(String skillCode, String versionNo, String excludeNum);
}
