package com.agent.ops.application.model.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.model.dto.ModelDTO;
import com.agent.ops.client.model.param.CreateModelParam;
import com.agent.ops.client.model.param.ModelActionParam;
import com.agent.ops.client.model.param.UpdateModelParam;
import com.agent.ops.domain.model.ModelAggregate;
import com.agent.ops.domain.model.factory.ModelFactory;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模型写应用服务。
 */
@Service
public class ModelCommandService {
    @Resource
    private ModelFactory modelFactory;

    @Resource
    private SecretEncryptor secretEncryptor;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 创建模型（草稿态）。
     *
     * @param param 入参
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO create(CreateModelParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("model:create:" + param.spaceCode + ":" + param.name, () -> {
            ModelAggregate a = modelFactory.create(param.spaceCode, param.name, param.modelId, param.baseUrl, param.apiKey, param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    /**
     * 修改模型（改字段：setter + save）。
     *
     * @param param 入参
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO update(UpdateModelParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.num, "num 不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("model:" + param.num, () -> {
            ModelAggregate a = loadAggregate(param.num);
            if (StrUtil.isNotBlank(param.name)) {
                a.setName(param.name);
            }
            if (StrUtil.isNotBlank(param.modelId)) {
                a.setModelId(param.modelId);
            }
            if (StrUtil.isNotBlank(param.baseUrl)) {
                a.setBaseUrl(param.baseUrl);
            }
            if (param.remark != null) {
                a.setRemark(param.remark);
            }
            // apiKey 处理：mask 占位/空字符串/已密文格式时不更新
            if (StrUtil.isNotBlank(param.apiKey)
                    && !secretEncryptor.isEncrypted(param.apiKey)
                    && !param.apiKey.contains("****")) {
                a.setApiKeyPlaintext(param.apiKey);
            }
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    /**
     * 启用模型。
     *
     * @param param 入参
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO enable(ModelActionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("model:" + param.num, () -> {
            ModelAggregate a = loadAggregate(param.num);
            a.enable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    /**
     * 禁用模型。
     *
     * @param param 入参
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO disable(ModelActionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("model:" + param.num, () -> {
            ModelAggregate a = loadAggregate(param.num);
            a.disable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    /**
     * 删除模型（仅草稿）。
     *
     * @param param 入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(ModelActionParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        distributedLock.run("model:" + param.num, () -> {
            ModelAggregate a = loadAggregate(param.num);
            a.delete(param.getOperatorCode());
        });
    }

    private ModelAggregate loadAggregate(String num) {
        ModelAggregate a = modelFactory.createByNum(num);
        if (a == null) {
            throw new BusinessException("MODEL_NOT_FOUND", "模型不存在");
        }
        return a;
    }

    private ModelDTO toDTO(ModelAggregate a) {
        ModelDTO dto = new ModelDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.modelId = a.getModelId();
        dto.baseUrl = a.getBaseUrl();
        dto.apiKey = StrUtil.isBlank(a.getApiKeyCipher()) ? null : secretEncryptor.mask(a.getApiKeyCipher());
        dto.remark = a.getRemark();
        dto.status = a.getStatus();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
