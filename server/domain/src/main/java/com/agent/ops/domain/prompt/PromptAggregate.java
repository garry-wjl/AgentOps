package com.agent.ops.domain.prompt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.prompt.enums.PromptStatus;
import com.agent.ops.domain.prompt.event.PromptEventConstant;
import com.agent.ops.domain.prompt.gateway.PromptGateway;
import com.agent.ops.domain.prompt.repository.PromptRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 聚合根。
 */
public class PromptAggregate extends DomainEntity {
    private static final String KEY_REGEX = "^[A-Za-z][A-Za-z0-9_-]{0,63}$";

    private String spaceCode;
    private String name;
    private String key;
    private String content;
    /**
     * 解析回写的变量列表。
     */
    private List<String> variables;
    private String remark;
    private PromptStatus status;

    private PromptRepository repository;
    private PromptGateway gateway;
    private DomainEventPublisher eventPublisher;

    public PromptAggregate() {
    }

    public PromptAggregate(PromptRepository repository, PromptGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = PromptStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generatePromptCode());
        }
        // 解析回写 variables
        if (StrUtil.isNotBlank(content)) {
            this.variables = gateway.extractVariables(content);
        } else {
            this.variables = new ArrayList<>();
        }
        // 启用/禁用态不允许改 key
        // 但本方法只关心持久化；key 校验放 domainValidate
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(PromptEventConstant.CREATED, operatorCode);
        }
    }

    /**
     * 提交：DRAFT → ENABLED。
     *
     * @param operatorCode 操作人
     */
    public void submit(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == PromptStatus.DRAFT, "仅草稿态可提交");
        Assert.notBlank(content, "内容不能为空");
        this.status = PromptStatus.ENABLED;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(PromptEventConstant.SUBMITTED, operatorCode);
    }

    /**
     * 启用：DISABLED → ENABLED。
     *
     * @param operatorCode 操作人
     */
    public void enable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == PromptStatus.DISABLED, "仅禁用态可启用");
        this.status = PromptStatus.ENABLED;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(PromptEventConstant.ENABLED, operatorCode);
    }

    /**
     * 禁用：ENABLED → DISABLED。
     *
     * @param operatorCode 操作人
     */
    public void disable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == PromptStatus.ENABLED, "仅启用态可禁用");
        this.status = PromptStatus.DISABLED;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(PromptEventConstant.DISABLED, operatorCode);
    }

    /**
     * 删除（仅 DRAFT）。
     *
     * @param operatorCode 操作人
     */
    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == PromptStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(PromptEventConstant.DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(StrUtil.length(name) <= 50, "name 长度不能超过 50 字符");
        Assert.notBlank(key, "key 不能为空");
        Assert.isTrue(ReUtil.isMatch(KEY_REGEX, key), "key 格式不合法（首字母 + 字母/数字/下划线/中划线，最多 64）");
        Assert.notBlank(content, "content 不能为空");
        Assert.isTrue(StrUtil.length(content) <= 10000, "content 长度不能超过 10000 字符");
        Assert.notNull(status, "status 不能为空");
        // 唯一性校验
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("名称已存在");
        }
        if (repository.existsByKey(spaceCode, key, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("Key 已存在");
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
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getVariables() { return variables == null ? new ArrayList<>() : variables; }
    public void setVariables(List<String> variables) {
        this.variables = variables == null ? new ArrayList<>() : new ArrayList<>(variables);
    }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public PromptStatus getStatus() { return status; }
    public void setStatus(PromptStatus status) { this.status = status; }
    public void setRepository(PromptRepository repository) { this.repository = repository; }
    public void setGateway(PromptGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }

    /** Hutool 的 {@link CollUtil} 在变量为空时返回空集合，避免 NPE。 */
    public boolean hasVariables() {
        return CollUtil.isNotEmpty(variables);
    }
}
