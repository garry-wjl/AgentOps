package com.agent.ops.application.skill.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.dto.SkillDTO;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.skill.param.CreateSkillParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.client.skill.param.UpdateSkillBasicParam;
import com.agent.ops.domain.skill.SkillAggregate;
import com.agent.ops.domain.skill.factory.SkillFactory;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillCommandService {
    @Resource
    private SkillFactory skillFactory;

    @Resource
    private SkillVersionCommandService skillVersionCommandService;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public SkillDTO create(CreateSkillParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        return distributedLock.execute("skill:create:" + param.spaceCode + ":" + param.name, () -> {
            SkillAggregate skill = skillFactory.create(param.spaceCode, param.name, param.description, param.tags, param.remark);
            skill.save(param.getOperatorCode());
            // 自动创建 V1 草稿版本
            String initialMd = StrUtil.blankToDefault(param.initialSkillMd,
                    "---\nname: " + param.name + "\ndescription: " + param.description + "\nversion: 1.0.0\n---\n# " + param.name + "\n");
            SkillVersionDTO versionDto = skillVersionCommandService.createInitialVersion(skill.getNum(), "1.0.0", initialMd, param.getOperatorCode());
            SkillDTO dto = toDTO(skill);
            dto.currentVersionNo = versionDto.versionNo;
            return dto;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillDTO updateBasic(UpdateSkillBasicParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("skill:" + param.num, () -> {
            SkillAggregate skill = loadAggregate(param.num);
            if (param.description != null) skill.setDescription(param.description);
            if (param.tags != null) skill.setTags(param.tags);
            if (param.remark != null) skill.setRemark(param.remark);
            skill.save(param.getOperatorCode());
            return toDTO(skill);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillDTO enable(SkillActionParam param) {
        return distributedLock.execute("skill:" + param.num, () -> {
            SkillAggregate skill = loadAggregate(param.num);
            skill.enable(param.getOperatorCode());
            return toDTO(skill);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillDTO withdraw(SkillActionParam param) {
        return distributedLock.execute("skill:" + param.num, () -> {
            SkillAggregate skill = loadAggregate(param.num);
            skill.withdraw(param.getOperatorCode());
            return toDTO(skill);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(SkillActionParam param) {
        distributedLock.run("skill:" + param.num, () -> {
            SkillAggregate skill = loadAggregate(param.num);
            skill.delete(param.getOperatorCode());
        });
    }

    /**
     * 由 SkillVersionPublishedListener 调用，刷新主体当前版本号。
     *
     * @param skillCode    skill 业务编码
     * @param versionNo    新版本号
     * @param operatorCode 操作人
     */
    @Transactional(rollbackFor = Exception.class)
    public void refreshCurrentVersion(String skillCode, String versionNo, String operatorCode) {
        SkillAggregate skill = skillFactory.createByNum(skillCode);
        if (skill == null) {
            return;
        }
        skill.setCurrentVersionNo(versionNo);
        skill.save(operatorCode);
    }

    private SkillAggregate loadAggregate(String num) {
        SkillAggregate skill = skillFactory.createByNum(num);
        if (skill == null) {
            throw new BusinessException("SKILL_NOT_FOUND", "Skill 不存在");
        }
        return skill;
    }

    private SkillDTO toDTO(SkillAggregate a) {
        SkillDTO dto = new SkillDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.description = a.getDescription();
        dto.currentVersionNo = a.getCurrentVersionNo();
        dto.status = a.getStatus();
        dto.tags = a.getTags();
        dto.remark = a.getRemark();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
