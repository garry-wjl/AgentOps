package com.agent.ops.application.prompt.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.prompt.dto.PromptDTO;
import com.agent.ops.client.prompt.enums.PromptStatus;
import com.agent.ops.client.prompt.param.CreatePromptParam;
import com.agent.ops.client.prompt.param.PromptActionParam;
import com.agent.ops.client.prompt.param.UpdatePromptParam;
import com.agent.ops.domain.prompt.PromptAggregate;
import com.agent.ops.domain.prompt.factory.PromptFactory;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromptCommandService {
    @Resource
    private PromptFactory promptFactory;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public PromptDTO create(CreatePromptParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("prompt:create:" + param.spaceCode + ":" + param.key, () -> {
            PromptAggregate a = promptFactory.create(param.spaceCode, param.name, param.key, param.content, param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public PromptDTO update(UpdatePromptParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("prompt:" + param.num, () -> {
            PromptAggregate a = loadAggregate(param.num);
            if (StrUtil.isNotBlank(param.name)) {
                a.setName(param.name);
            }
            if (param.content != null) {
                a.setContent(param.content);
            }
            if (param.remark != null) {
                a.setRemark(param.remark);
            }
            // 仅草稿态可改 key
            if (StrUtil.isNotBlank(param.newKey)) {
                if (a.getStatus() != PromptStatus.DRAFT) {
                    throw new BusinessException("PROMPT_KEY_LOCKED", "仅草稿态可修改 Key");
                }
                a.setKey(param.newKey);
            }
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public PromptDTO submit(PromptActionParam param) {
        return distributedLock.execute("prompt:" + param.num, () -> {
            PromptAggregate a = loadAggregate(param.num);
            a.submit(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public PromptDTO enable(PromptActionParam param) {
        return distributedLock.execute("prompt:" + param.num, () -> {
            PromptAggregate a = loadAggregate(param.num);
            a.enable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public PromptDTO disable(PromptActionParam param) {
        return distributedLock.execute("prompt:" + param.num, () -> {
            PromptAggregate a = loadAggregate(param.num);
            a.disable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(PromptActionParam param) {
        distributedLock.run("prompt:" + param.num, () -> {
            PromptAggregate a = loadAggregate(param.num);
            a.delete(param.getOperatorCode());
        });
    }

    private PromptAggregate loadAggregate(String num) {
        PromptAggregate a = promptFactory.createByNum(num);
        if (a == null) {
            throw new BusinessException("PROMPT_NOT_FOUND", "Prompt 不存在");
        }
        return a;
    }

    private PromptDTO toDTO(PromptAggregate a) {
        PromptDTO dto = new PromptDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.key = a.getKey();
        dto.content = a.getContent();
        dto.variables = a.getVariables();
        dto.remark = a.getRemark();
        dto.status = a.getStatus();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
