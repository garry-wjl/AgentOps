package com.agent.ops.domain.space;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.space.enums.SpaceStatus;
import com.agent.ops.domain.space.event.SpaceEventConstant;
import com.agent.ops.domain.space.gateway.SpaceGateway;
import com.agent.ops.domain.space.repository.SpaceRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 空间领域聚合根。
 * <p>
 * 不变量：
 * <ul>
 * <li>name 在平台内唯一（含软删除过滤），1~50 字符</li>
 * <li>ownerUserCode 必须出现在 adminUserCodes 中</li>
 * <li>adminUserCodes 与 memberUserCodes 无交集</li>
 * <li>任意列表内无重复 userCode</li>
 * <li>总成员数（admin + member）≤ 500</li>
 * </ul>
 * 领域动作仅 {@link #save(String)} 与 {@link #delete(String)}；其余字段修改由应用层 setter + save 完成。
 */
@Getter
@Setter
public class SpaceAggregate extends DomainEntity {
    /**
     * 名称最大长度。
     */
    private static final int MAX_NAME_LENGTH = 50;

    /**
     * 描述最大长度。
     */
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    /**
     * Icon URL 最大长度。
     */
    private static final int MAX_ICON_URL_LENGTH = 500;

    /**
     * 单个空间总成员数上限（admin + member）。
     */
    private static final int MAX_MEMBER_TOTAL = 500;

    /**
     * 空间名称。
     */
    private String name;

    /**
     * 空间描述。
     */
    private String description;

    /**
     * Logo URL。
     */
    private String iconUrl;

    /**
     * 空间所有者用户业务编码（user.num）。一经创建不可修改。
     */
    private String ownerUserCode;

    /**
     * 空间生命周期状态。
     */
    private SpaceStatus status;

    /**
     * 管理员用户业务编码列表（含 owner）。
     */
    private List<String> adminUserCodes;

    /**
     * 普通成员用户业务编码列表。
     */
    private List<String> memberUserCodes;

    /**
     * 空间聚合仓储。
     */
    private SpaceRepository repository;

    /**
     * 空间领域网关。
     */
    private SpaceGateway gateway;

    /**
     * 领域事件发布器。
     */
    private DomainEventPublisher eventPublisher;

    /**
     * 默认构造，供仓储重建领域对象时使用。
     */
    public SpaceAggregate() {
    }

    /**
     * 构造空间聚合并注入领域协作依赖。
     *
     * @param repository     空间聚合仓储
     * @param gateway        空间领域网关
     * @param eventPublisher 领域事件发布器
     */
    public SpaceAggregate(SpaceRepository repository, SpaceGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 持久化空间聚合。新建时业务编码可空，由本方法生成；新建时自动把 owner 加入 adminUserCodes。
     *
     * @param operatorCode 当前操作人业务编码
     */
    @Override
    public void save(String operatorCode) {
        // 1. 校验领域协作依赖。
        assertCollaboratorsReady();
        // 2. 默认值与编码生成。
        boolean isNew = (getId() == null);
        initializeDefaults();
        ensureOwnerInAdmin();
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateSpaceCode());
        }
        // 3. 审计字段。
        initialize(operatorCode);
        // 4. 完整性校验（domainValidate 内调）。
        validate();
        // 5. 持久化。
        repository.save(this);
        // 6. 仅新建发 created 事件。
        if (isNew) {
            publishEvent(SpaceEventConstant.SPACE_CREATED, operatorCode);
        }
    }

    /**
     * 软删除空间。
     *
     * @param operatorCode 当前操作人业务编码
     */
    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.notBlank(operatorCode, "operatorCode must not be blank");
        Assert.notBlank(getNum(), "空间业务编码不能为空");
        // 仅 owner 可删除（平台管理员校验在应用层完成）。
        Assert.isTrue(StrUtil.equals(operatorCode, ownerUserCode), "仅空间所有者可删除空间");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(SpaceEventConstant.SPACE_DELETED, operatorCode);
    }

    /**
     * 校验空间聚合的业务不变量。
     */
    @Override
    public void domainValidate() {
        Assert.notBlank(name, "空间名称不能为空");
        Assert.isTrue(StrUtil.length(name) <= MAX_NAME_LENGTH, "空间名称长度不能超过 " + MAX_NAME_LENGTH + " 字符");
        if (StrUtil.isNotBlank(description)) {
            Assert.isTrue(StrUtil.length(description) <= MAX_DESCRIPTION_LENGTH, "空间描述长度不能超过 " + MAX_DESCRIPTION_LENGTH + " 字符");
        }
        if (StrUtil.isNotBlank(iconUrl)) {
            Assert.isTrue(StrUtil.length(iconUrl) <= MAX_ICON_URL_LENGTH, "Logo URL 长度不能超过 " + MAX_ICON_URL_LENGTH + " 字符");
        }
        Assert.notBlank(ownerUserCode, "空间所有者业务编码不能为空");
        Assert.notNull(status, "空间状态不能为空");
        validateMemberLists();
        // 唯一性：仅在确实变更名称（或新建）时校验，避免每次 save 触发额外查询；这里委托 repository 做查询。
        Assert.notNull(repository, "空间仓储不能为空");
        if (repository.existsByName(name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("空间名称已存在");
        }
    }

    /**
     * 初始化默认字段。
     */
    private void initializeDefaults() {
        if (status == null) {
            status = SpaceStatus.ENABLED;
        }
        if (adminUserCodes == null) {
            adminUserCodes = new ArrayList<>();
        }
        if (memberUserCodes == null) {
            memberUserCodes = new ArrayList<>();
        }
    }

    /**
     * 确保 owner 在 adminUserCodes 列表中（去重）。
     */
    private void ensureOwnerInAdmin() {
        if (StrUtil.isBlank(ownerUserCode)) {
            return;
        }
        if (!adminUserCodes.contains(ownerUserCode)) {
            adminUserCodes.add(ownerUserCode);
        }
    }

    /**
     * 校验成员列表的不变量。
     */
    private void validateMemberLists() {
        Assert.notNull(adminUserCodes, "管理员列表不能为空");
        Assert.notNull(memberUserCodes, "普通成员列表不能为空");
        // owner 必在 admin 列表中。
        Assert.isTrue(adminUserCodes.contains(ownerUserCode), "空间所有者必须在管理员列表中");
        // 列表内无重复。
        Assert.isTrue(new HashSet<>(adminUserCodes).size() == adminUserCodes.size(), "管理员列表存在重复用户");
        Assert.isTrue(new HashSet<>(memberUserCodes).size() == memberUserCodes.size(), "普通成员列表存在重复用户");
        // admin 与 member 无交集。
        Set<String> intersect = new HashSet<>(adminUserCodes);
        intersect.retainAll(memberUserCodes);
        Assert.isTrue(intersect.isEmpty(), "同一用户不能同时是管理员和普通成员");
        // 总数上限。
        int total = adminUserCodes.size() + memberUserCodes.size();
        Assert.isTrue(total <= MAX_MEMBER_TOTAL, "单个空间成员数不能超过 " + MAX_MEMBER_TOTAL);
    }

    /**
     * 应用层在加成员后通过此方法发出 added 事件。
     *
     * @param userCode     被加入的用户业务编码
     * @param roleType     角色类型字符串（ADMIN/MEMBER）
     * @param operatorCode 当前操作人
     */
    public void publishMemberAdded(String userCode, String roleType, String operatorCode) {
        publishMemberEvent(SpaceEventConstant.SPACE_MEMBER_ADDED, userCode, roleType, operatorCode);
    }

    /**
     * 应用层在移除成员后通过此方法发出 removed 事件。
     *
     * @param userCode     被移除的用户业务编码
     * @param operatorCode 当前操作人
     */
    public void publishMemberRemoved(String userCode, String operatorCode) {
        publishMemberEvent(SpaceEventConstant.SPACE_MEMBER_REMOVED, userCode, null, operatorCode);
    }

    /**
     * 应用层在改角色后通过此方法发出 role_changed 事件。
     *
     * @param userCode     被修改的用户业务编码
     * @param roleType     新角色类型字符串
     * @param operatorCode 当前操作人
     */
    public void publishMemberRoleChanged(String userCode, String roleType, String operatorCode) {
        publishMemberEvent(SpaceEventConstant.SPACE_MEMBER_ROLE_CHANGED, userCode, roleType, operatorCode);
    }

    /**
     * 校验领域协作依赖是否齐备。
     */
    private void assertCollaboratorsReady() {
        Assert.notNull(repository, "空间仓储不能为空");
        Assert.notNull(gateway, "空间领域网关不能为空");
        Assert.notNull(eventPublisher, "领域事件发布器不能为空");
    }

    /**
     * 发布单个领域事件。
     *
     * @param eventType    事件类型
     * @param operatorCode 当前操作人
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
     * 发布成员相关领域事件。
     *
     * @param eventType    事件类型
     * @param userCode     成员用户业务编码
     * @param roleType     角色类型字符串（可空）
     * @param operatorCode 当前操作人
     */
    private void publishMemberEvent(String eventType, String userCode, String roleType, String operatorCode) {
        Assert.notNull(eventPublisher, "领域事件发布器不能为空");
        Assert.notBlank(userCode, "成员业务编码不能为空");
        DomainEventDTO event = new DomainEventDTO();
        event.setEventId(IdUtil.fastSimpleUUID());
        event.setEventType(eventType);
        event.setBusinessNum(getNum());
        event.setOccurredAt(LocalDateTimeUtil.now());
        event.setOperatorCode(operatorCode);
        event.setPayload(CollUtil.newArrayList(userCode, roleType));
        eventPublisher.publish(event);
    }

    /**
     * 返回 name。
     *
     * @return 空间名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 name。
     *
     * @param name 空间名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回 description。
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置 description。
     *
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 返回 iconUrl。
     *
     * @return Logo URL
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * 设置 iconUrl。
     *
     * @param iconUrl Logo URL
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * 返回 ownerUserCode。
     *
     * @return 空间所有者业务编码
     */
    public String getOwnerUserCode() {
        return ownerUserCode;
    }

    /**
     * 设置 ownerUserCode。仅工厂或仓储重建时使用，业务上不应在生命周期内变更。
     *
     * @param ownerUserCode 空间所有者业务编码
     */
    public void setOwnerUserCode(String ownerUserCode) {
        this.ownerUserCode = ownerUserCode;
    }

    /**
     * 返回 status。
     *
     * @return 状态
     */
    public SpaceStatus getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status 状态
     */
    public void setStatus(SpaceStatus status) {
        this.status = status;
    }

    /**
     * 返回 adminUserCodes。
     *
     * @return 管理员业务编码列表
     */
    public List<String> getAdminUserCodes() {
        return adminUserCodes;
    }

    /**
     * 设置 adminUserCodes。
     *
     * @param adminUserCodes 管理员业务编码列表
     */
    public void setAdminUserCodes(List<String> adminUserCodes) {
        this.adminUserCodes = adminUserCodes;
    }

    /**
     * 返回 memberUserCodes。
     *
     * @return 普通成员业务编码列表
     */
    public List<String> getMemberUserCodes() {
        return memberUserCodes;
    }

    /**
     * 设置 memberUserCodes。
     *
     * @param memberUserCodes 普通成员业务编码列表
     */
    public void setMemberUserCodes(List<String> memberUserCodes) {
        this.memberUserCodes = memberUserCodes;
    }

    /**
     * 返回 repository。
     *
     * @return 空间仓储
     */
    public SpaceRepository getRepository() {
        return repository;
    }

    /**
     * 设置 repository。
     *
     * @param repository 空间仓储
     */
    public void setRepository(SpaceRepository repository) {
        this.repository = repository;
    }

    /**
     * 返回 gateway。
     *
     * @return 空间领域网关
     */
    public SpaceGateway getGateway() {
        return gateway;
    }

    /**
     * 设置 gateway。
     *
     * @param gateway 空间领域网关
     */
    public void setGateway(SpaceGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * 返回 eventPublisher。
     *
     * @return 领域事件发布器
     */
    public DomainEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    /**
     * 设置 eventPublisher。
     *
     * @param eventPublisher 领域事件发布器
     */
    public void setEventPublisher(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
