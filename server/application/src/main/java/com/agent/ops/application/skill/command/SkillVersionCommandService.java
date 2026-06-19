package com.agent.ops.application.skill.command;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.skill.param.DeriveVersionParam;
import com.agent.ops.client.skill.param.EditVersionParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.domain.skill.SkillResourceFileAggregate;
import com.agent.ops.domain.skill.SkillVersionAggregate;
import com.agent.ops.domain.skill.factory.SkillResourceFileFactory;
import com.agent.ops.domain.skill.factory.SkillVersionFactory;
import com.agent.ops.domain.skill.repository.SkillResourceFileRepository;
import com.agent.ops.domain.skill.repository.SkillVersionRepository;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillVersionCommandService {
    @Resource
    private SkillVersionFactory skillVersionFactory;

    @Resource
    private SkillResourceFileFactory skillResourceFileFactory;

    @Resource
    private SkillVersionRepository skillVersionRepository;

    @Resource
    private SkillResourceFileRepository skillResourceFileRepository;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 创建初始 V1 版本（仅 SkillCommandService 内部调用）。
     *
     * @param skillCode      Skill 业务编码
     * @param versionNo      版本号
     * @param skillMdContent 初始 SkillMD
     * @param operatorCode   操作人
     * @return 版本 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SkillVersionDTO createInitialVersion(String skillCode, String versionNo, String skillMdContent, String operatorCode) {
        SkillVersionAggregate v = skillVersionFactory.create(skillCode, versionNo, skillMdContent);
        v.save(operatorCode);
        return toDTO(v);
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillVersionDTO deriveDraft(DeriveVersionParam param) {
        Assert.notBlank(param.skillCode, "skillCode 不能为空");
        Assert.notBlank(param.sourceVersionCode, "sourceVersionCode 不能为空");
        Assert.notBlank(param.newVersionNo, "newVersionNo 不能为空");
        return distributedLock.execute("skill_version:derive:" + param.skillCode, () -> {
            // 同一 Skill 同时只能有一个 DRAFT
            SkillVersionAggregate existingDraft = skillVersionRepository.findDraftBySkillCode(param.skillCode);
            if (existingDraft != null) {
                return toDTO(existingDraft);
            }
            // 复制源版本的 SkillMD 与资源文件
            SkillVersionAggregate source = skillVersionFactory.createByNum(param.sourceVersionCode);
            if (source == null) {
                throw new BusinessException("SKILL_VERSION_NOT_FOUND", "源版本不存在");
            }
            String mdWithNewVersion = replaceFrontmatterVersion(source.getSkillMdContent(), param.newVersionNo);
            SkillVersionAggregate v = skillVersionFactory.create(param.skillCode, param.newVersionNo, mdWithNewVersion);
            v.save(param.getOperatorCode());
            // 复制资源文件
            List<SkillResourceFileAggregate> sources = skillResourceFileRepository.listByVersionCode(source.getNum());
            for (SkillResourceFileAggregate src : sources) {
                SkillResourceFileAggregate copy = skillResourceFileFactory.create(v.getNum(), src.getPath(), src.getType(), src.getContent());
                copy.save(param.getOperatorCode());
            }
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillVersionDTO editContent(EditVersionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("skill_version:" + param.num, () -> {
            SkillVersionAggregate v = loadAggregate(param.num);
            v.setSkillMdContent(param.skillMdContent);
            v.save(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillVersionDTO publish(SkillActionParam param) {
        return distributedLock.execute("skill_version:" + param.num, () -> {
            SkillVersionAggregate v = loadAggregate(param.num);
            v.publish(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillVersionDTO withdraw(SkillActionParam param) {
        return distributedLock.execute("skill_version:" + param.num, () -> {
            SkillVersionAggregate v = loadAggregate(param.num);
            v.withdraw(param.getOperatorCode());
            return toDTO(v);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(SkillActionParam param) {
        distributedLock.run("skill_version:" + param.num, () -> {
            SkillVersionAggregate v = loadAggregate(param.num);
            v.delete(param.getOperatorCode());
        });
    }

    private SkillVersionAggregate loadAggregate(String num) {
        SkillVersionAggregate v = skillVersionFactory.createByNum(num);
        if (v == null) {
            throw new BusinessException("SKILL_VERSION_NOT_FOUND", "Skill 版本不存在");
        }
        return v;
    }

    private String replaceFrontmatterVersion(String md, String newVersion) {
        if (md == null) return null;
        return md.replaceFirst("(?m)^version:\\s*.*$", "version: " + newVersion);
    }

    private SkillVersionDTO toDTO(SkillVersionAggregate v) {
        SkillVersionDTO dto = new SkillVersionDTO();
        dto.num = v.getNum();
        dto.skillCode = v.getSkillCode();
        dto.versionNo = v.getVersionNo();
        dto.skillMdContent = v.getSkillMdContent();
        dto.status = v.getStatus();
        dto.publishTime = v.getPublishTime();
        dto.withdrawTime = v.getWithdrawTime();
        dto.createTime = v.getCreateTime();
        dto.updateTime = v.getUpdateTime();
        return dto;
    }
}
