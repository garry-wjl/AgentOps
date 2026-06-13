package com.agent.ops.domain.model;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.model.enums.ModelStatus;
import com.agent.ops.domain.model.event.ModelEventConstant;
import com.agent.ops.domain.model.gateway.ModelGateway;
import com.agent.ops.domain.model.repository.ModelRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

/**
 * 模型聚合根。仅 enable/disable/save/delete 四个方法（公共方案 §11.5）。
 */
public class ModelAggregate extends DomainEntity {
    /**
     * 所属空间业务编码。
     */
    private String spaceCode;

    /**
     * 名称（空间内唯一）。
     */
    private String name;

    /**
     * 模型标识（调用 LLM 时传给供应商的 model 参数）。
     */
    private String modelId;

    /**
     * Base URL。
     */
    private String baseUrl;

    /**
     * API Key 密文（enc:v1:...）。
     */
    private String apiKeyCipher;

    /**
     * 应用层在 setApiKeyPlaintext 时持有的明文，save 时由 Gateway 加密。
     */
    private transient String apiKeyPlaintext;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 状态。
     */
    private ModelStatus status;

    private ModelRepository repository;
    private ModelGateway gateway;
    private DomainEventPublisher eventPublisher;

    public ModelAggregate() {
    }

    public ModelAggregate(ModelRepository repository, ModelGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 持久化。
     *
     * @param operatorCode 操作人
     */
    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = ModelStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateModelCode());
        }
        // 应用层注入明文则加密；脱敏占位/空值则保留原密文。
        if (StrUtil.isNotBlank(apiKeyPlaintext)) {
            this.apiKeyCipher = gateway.encrypt(apiKeyPlaintext);
            this.apiKeyPlaintext = null;
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(ModelEventConstant.CREATED, operatorCode);
        }
    }

    /**
     * 启用：DRAFT/DISABLED → ENABLED。
     *
     * @param operatorCode 操作人
     */
    public void enable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ModelStatus.DRAFT || status == ModelStatus.DISABLED,
                "仅草稿或禁用状态可启用");
        Assert.notBlank(modelId, "modelId 不能为空");
        Assert.notBlank(baseUrl, "baseUrl 不能为空");
        Assert.notBlank(apiKeyCipher, "apiKey 不能为空");
        this.status = ModelStatus.ENABLED;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(ModelEventConstant.ENABLED, operatorCode);
    }

    /**
     * 禁用：ENABLED → DISABLED。
     *
     * @param operatorCode 操作人
     */
    public void disable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ModelStatus.ENABLED, "仅启用状态可禁用");
        this.status = ModelStatus.DISABLED;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(ModelEventConstant.DISABLED, operatorCode);
    }

    /**
     * 删除：仅 DRAFT 可删。
     *
     * @param operatorCode 操作人
     */
    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ModelStatus.DRAFT, "仅草稿状态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(ModelEventConstant.DELETED, operatorCode);
    }

    /**
     * 业务不变量校验。
     */
    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(StrUtil.length(name) <= 50, "name 长度不能超过 50 字符");
        Assert.notBlank(modelId, "modelId 不能为空");
        Assert.isTrue(StrUtil.length(modelId) <= 100, "modelId 长度不能超过 100 字符");
        Assert.notBlank(baseUrl, "baseUrl 不能为空");
        Assert.isTrue(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"),
                "baseUrl 必须以 http:// 或 https:// 开头");
        Assert.notNull(status, "status 不能为空");
        // 唯一性校验
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("模型名称已存在");
        }
        if (repository.existsByModelId(spaceCode, modelId, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("模型标识已存在");
        }
    }

    private void assertCollaboratorsReady() {
        Assert.notNull(repository, "repository 不能为空");
        Assert.notNull(gateway, "gateway 不能为空");
        Assert.notNull(eventPublisher, "eventPublisher 不能为空");
    }

    private void publishEvent(String eventType, String operatorCode) {
        DomainEventDTO event = new DomainEventDTO();
        event.setEventId(IdUtil.fastSimpleUUID());
        event.setEventType(eventType);
        event.setBusinessNum(getNum());
        event.setOccurredAt(LocalDateTimeUtil.now());
        event.setOperatorCode(operatorCode);
        event.setPayload(this);
        eventPublisher.publish(event);
    }

    public String getSpaceCode() { return spaceCode; }
    public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKeyCipher() { return apiKeyCipher; }
    public void setApiKeyCipher(String apiKeyCipher) { this.apiKeyCipher = apiKeyCipher; }
    public String getApiKeyPlaintext() { return apiKeyPlaintext; }
    public void setApiKeyPlaintext(String apiKeyPlaintext) { this.apiKeyPlaintext = apiKeyPlaintext; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public ModelStatus getStatus() { return status; }
    public void setStatus(ModelStatus status) { this.status = status; }
    public void setRepository(ModelRepository repository) { this.repository = repository; }
    public void setGateway(ModelGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
