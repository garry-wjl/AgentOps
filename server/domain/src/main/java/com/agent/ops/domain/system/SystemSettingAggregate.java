package com.agent.ops.domain.system;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.event.SystemEventConstant;
import com.agent.ops.domain.system.gateway.SystemSettingGateway;
import com.agent.ops.domain.system.repository.SystemSettingRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

/**
 * 系统设置聚合根。一个分类对应一个聚合实例；settingJson 为该分类的完整 JSON 字符串。
 */
public class SystemSettingAggregate extends DomainEntity {
    /**
     * 分类（platform_basic / smtp / space_policy / sandbox_default）。一经创建不可修改。
     */
    private String category;

    /**
     * 设置 JSON。
     */
    private String settingJson;

    /**
     * 仓储。
     */
    private SystemSettingRepository repository;

    /**
     * 网关。
     */
    private SystemSettingGateway gateway;

    /**
     * 事件发布器。
     */
    private DomainEventPublisher eventPublisher;

    /**
     * 默认构造。
     */
    public SystemSettingAggregate() {
    }

    /**
     * 注入领域协作依赖。
     *
     * @param repository     仓储
     * @param gateway        网关
     * @param eventPublisher 事件发布器
     */
    public SystemSettingAggregate(SystemSettingRepository repository, SystemSettingGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 持久化（新建或更新）。
     * <p>
     * - 新建（id == null）：发 created 事件
     * - 更新且 settingJson 变化：发 changed 事件（载荷含 category，由 SystemSettingsLoader 订阅刷新缓存）
     * - 更新但 settingJson 未变：仅持久化 update_time 等审计字段
     *
     * @param operatorCode 当前操作人
     */
    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        Assert.notBlank(category, "category 不能为空");
        Assert.notBlank(settingJson, "settingJson 不能为空");
        boolean isNew = (getId() == null);
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateSettingCode());
        }
        // 检测内容变化（仅在更新时）
        boolean contentChanged = false;
        if (!isNew) {
            SystemSettingAggregate existing = repository.findByNum(getNum());
            if (existing != null && !StrUtil.equals(existing.settingJson, settingJson)) {
                contentChanged = true;
            }
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(SystemEventConstant.SETTING_CREATED, operatorCode);
        } else if (contentChanged) {
            publishEvent(SystemEventConstant.SETTING_CHANGED, operatorCode);
        }
    }

    /**
     * 软删除。
     *
     * @param operatorCode 当前操作人
     */
    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.notBlank(getNum(), "num 不能为空");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(SystemEventConstant.SETTING_DELETED, operatorCode);
    }

    /**
     * 校验业务不变量。
     */
    @Override
    public void domainValidate() {
        Assert.notBlank(category, "category 不能为空");
        Assert.notBlank(settingJson, "settingJson 不能为空");
    }

    /**
     * 校验领域协作依赖齐备。
     */
    private void assertCollaboratorsReady() {
        Assert.notNull(repository, "仓储不能为空");
        Assert.notNull(gateway, "网关不能为空");
        Assert.notNull(eventPublisher, "事件发布器不能为空");
    }

    /**
     * 发布事件。
     *
     * @param eventType    事件类型
     * @param operatorCode 操作人
     */
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

    /**
     * 返回 category。
     *
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置 category。
     *
     * @param category 分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 返回 settingJson。
     *
     * @return settingJson
     */
    public String getSettingJson() {
        return settingJson;
    }

    /**
     * 设置 settingJson。
     *
     * @param settingJson JSON
     */
    public void setSettingJson(String settingJson) {
        this.settingJson = settingJson;
    }

    /**
     * 设置 repository。
     *
     * @param repository 仓储
     */
    public void setRepository(SystemSettingRepository repository) {
        this.repository = repository;
    }

    /**
     * 设置 gateway。
     *
     * @param gateway 网关
     */
    public void setGateway(SystemSettingGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * 设置 eventPublisher。
     *
     * @param eventPublisher 事件发布器
     */
    public void setEventPublisher(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
