package com.agent.ops.application.skill.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.dto.SkillResourceFileDTO;
import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.client.skill.enums.SkillVersionStatus;
import com.agent.ops.client.skill.param.CreateResourceFileParam;
import com.agent.ops.client.skill.param.RenameResourceFileParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.client.skill.param.UpdateResourceFileContentParam;
import com.agent.ops.domain.skill.SkillResourceFileAggregate;
import com.agent.ops.domain.skill.SkillVersionAggregate;
import com.agent.ops.domain.skill.factory.SkillResourceFileFactory;
import com.agent.ops.domain.skill.factory.SkillVersionFactory;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillResourceFileCommandService {
    @Resource
    private SkillResourceFileFactory skillResourceFileFactory;

    @Resource
    private SkillVersionFactory skillVersionFactory;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public SkillResourceFileDTO create(CreateResourceFileParam param) {
        Assert.notBlank(param.versionCode, "versionCode 不能为空");
        Assert.notBlank(param.path, "path 不能为空");
        Assert.notNull(param.type, "type 不能为空");
        return distributedLock.execute("skill_resource:" + param.versionCode + ":" + param.path, () -> {
            assertVersionDraft(param.versionCode);
            FileType type = param.type;
            SkillResourceFileAggregate file = skillResourceFileFactory.create(param.versionCode, param.path, type, param.content);
            file.save(param.getOperatorCode());
            return toDTO(file);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillResourceFileDTO updateContent(UpdateResourceFileContentParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("skill_resource:" + param.num, () -> {
            SkillResourceFileAggregate file = loadAggregate(param.num);
            assertVersionDraft(file.getSkillVersionCode());
            file.setContent(param.content);
            // sizeBytes 由 save 内部重新计算
            file.setSizeBytes(null);
            file.save(param.getOperatorCode());
            return toDTO(file);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SkillResourceFileDTO rename(RenameResourceFileParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        Assert.notBlank(param.newPath, "newPath 不能为空");
        return distributedLock.execute("skill_resource:" + param.num, () -> {
            SkillResourceFileAggregate file = loadAggregate(param.num);
            assertVersionDraft(file.getSkillVersionCode());
            file.setPath(param.newPath);
            file.save(param.getOperatorCode());
            return toDTO(file);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(SkillActionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        distributedLock.run("skill_resource:" + param.num, () -> {
            SkillResourceFileAggregate file = loadAggregate(param.num);
            assertVersionDraft(file.getSkillVersionCode());
            file.delete(param.getOperatorCode());
        });
    }

    private void assertVersionDraft(String versionCode) {
        SkillVersionAggregate v = skillVersionFactory.createByNum(versionCode);
        if (v == null) {
            throw new BusinessException("SKILL_VERSION_NOT_FOUND", "Skill 版本不存在");
        }
        if (v.getStatus() != SkillVersionStatus.DRAFT) {
            throw new BusinessException("SKILL_VERSION_NOT_DRAFT", "仅草稿态版本可修改资源文件");
        }
    }

    private SkillResourceFileAggregate loadAggregate(String num) {
        SkillResourceFileAggregate file = skillResourceFileFactory.createByNum(num);
        if (file == null) {
            throw new BusinessException("SKILL_RESOURCE_NOT_FOUND", "资源文件不存在");
        }
        return file;
    }

    private SkillResourceFileDTO toDTO(SkillResourceFileAggregate a) {
        SkillResourceFileDTO dto = new SkillResourceFileDTO();
        dto.num = a.getNum();
        dto.skillVersionCode = a.getSkillVersionCode();
        dto.path = a.getPath();
        dto.type = a.getType();
        dto.content = a.getContent();
        dto.sizeBytes = a.getSizeBytes();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
