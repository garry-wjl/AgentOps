package com.agent.ops.domain.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.common.DomainEventConstant;
import com.agent.ops.domain.user.gateway.UserGateway;
import com.agent.ops.domain.user.repository.UserRepository;
import com.agent.ops.domain.user.valueobject.PasswordCredential;
import com.agent.ops.domain.user.valueobject.UserRole;
import com.agent.ops.domain.user.valueobject.UserStatus;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户领域聚合根，封装平台用户资料、状态、角色与密码凭证的业务规则。
 */
@Getter
@Setter
public class UserAggregate extends DomainEntity {
    /**
     * 邮箱登录账号。
     */
    private String email;

    /**
     * 手机号登录账号。
     */
    private String phone;

    /**
     * 用户姓名或展示名称。
     */
    private String name;

    /**
     * 用户平台角色列表。
     */
    private List<UserRole> roles;

    /**
     * 用户生命周期状态。
     */
    private UserStatus status;

    /**
     * 用户密码凭证。
     */
    private PasswordCredential credential;

    /**
     * 用户备注。
     */
    private String remark;

    /**
     * 用户聚合仓储。
     */
    private UserRepository userRepository;

    /**
     * 用户领域网关。
     */
    private UserGateway userGateway;

    /**
     * 领域事件发布器。
     */
    private DomainEventPublisher domainEventPublisher;

    /**
     * 创建空用户聚合，供仓储层重建领域对象时使用。
     */
    public UserAggregate() {
    }

    /**
     * 创建用户聚合并注入领域协作依赖。
     *
     * @param userRepository 用户聚合仓储
     * @param userGateway 用户领域网关
     * @param domainEventPublisher 领域事件发布器
     */
    public UserAggregate(UserRepository userRepository, UserGateway userGateway, DomainEventPublisher domainEventPublisher) {
        this.userRepository = userRepository;
        this.userGateway = userGateway;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 保存用户聚合，常用于创建空草稿或持久化当前聚合状态。
     *
     * @param operatorCode 当前操作人标识
     */
    @Override
    public void save(String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        // 3. 赋值：初始化默认值、生成业务编码，并在资料已填写时校验资料唯一性。
        initializeDefaults();
        generateNumIfBlank();
        validateProfileIfPresent();
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象：新增和更新统一调用 save。
        userRepository.save(this);
        // 6. 发布领域事件：每次保存都发布保存事件。
        publishEvent(DomainEventConstant.USER_SAVED, operatorCode);
    }

    /**
     * 删除草稿态用户。
     *
     * @param operatorCode 当前操作人标识
     */
    @Override
    public void delete(String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.DRAFT == status, "仅草稿态用户允许删除");
        // 3. 赋值：删除操作不在领域对象中维护软删除标记。
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象：删除操作按仓储三方法契约调用 deleteByNum。
        userRepository.deleteByNum(getNum());
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_DELETED, operatorCode);
    }

    /**
     * 将草稿态用户提交为启用态用户。
     *
     * @param operatorCode 当前操作人标识
     */
    public void submit(String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.DRAFT == status, "仅草稿态用户允许提交");
        assertProfileComplete();
        // 3. 赋值：状态从草稿流转为启用。
        this.status = UserStatus.ENABLED;
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象。
        userRepository.save(this);
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_SUBMITTED, operatorCode);
    }

    /**
     * 启用禁用态用户。
     *
     * @param operatorCode 当前操作人标识
     */
    public void enable(String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.DISABLED == status, "仅禁用态用户允许启用");
        // 3. 赋值：状态从禁用流转为启用。
        this.status = UserStatus.ENABLED;
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象。
        userRepository.save(this);
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_ENABLED, operatorCode);
    }

    /**
     * 禁用启用态用户。
     *
     * @param operatorCode 当前操作人标识
     */
    public void disable(String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.ENABLED == status, "仅启用态用户允许禁用");
        // 3. 赋值：状态从启用流转为禁用。
        this.status = UserStatus.DISABLED;
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象。
        userRepository.save(this);
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_DISABLED, operatorCode);
    }

    /**
     * 重置启用或禁用用户的密码凭证。
     *
     * @param passwordHash 新密码哈希值
     * @param operatorCode 当前操作人标识
     */
    public void resetPassword(String passwordHash, String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.DRAFT != status, "草稿态用户不允许重置密码");
        // 3. 赋值：重置密码凭证。
        initializeDefaults();
        this.credential.reset(passwordHash, operatorCode);
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象。
        userRepository.save(this);
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_PASSWORD_RESET, operatorCode);
    }

    /**
     * 为草稿态或启用态用户分配平台角色。
     *
     * @param roles 平台角色列表
     * @param operatorCode 当前操作人标识
     */
    public void assignRoles(List<UserRole> roles, String operatorCode) {
        // 1. 初始化对象。
        initialize(operatorCode);
        // 2. 领域规则校验。
        assertCollaboratorsReady();
        Assert.isTrue(UserStatus.DRAFT == status || UserStatus.ENABLED == status, "仅草稿态或启用态用户允许分配角色");
        validateRoles(roles);
        // 3. 赋值：更新平台角色列表。
        this.roles = new ArrayList<>(roles);
        // 4. 领域完整性校验。
        validate();
        // 5. 持久化对象。
        userRepository.save(this);
        // 6. 发布领域事件。
        publishEvent(DomainEventConstant.USER_ROLES_ASSIGNED, operatorCode);
    }

    /**
     * 校验用户聚合整体业务完整性。
     */
    @Override
    public void domainValidate() {
        assertCollaboratorsReady();
        Assert.notBlank(getNum(), "用户业务编码不能为空");
        Assert.notNull(status, "用户状态不能为空");
        if (UserStatus.ENABLED == status || UserStatus.DISABLED == status) {
            assertProfileComplete();
        }
        if (StrUtil.isNotBlank(remark)) {
            Assert.isTrue(StrUtil.length(remark) <= 200, "备注不能超过 200 字");
        }
    }

    /**
     * 校验领域协作依赖是否已注入。
     */
    private void assertCollaboratorsReady() {
        Assert.notNull(userRepository, "用户仓储不能为空");
        Assert.notNull(userGateway, "用户领域网关不能为空");
        Assert.notNull(domainEventPublisher, "领域事件发布器不能为空");
    }

    /**
     * 初始化用户聚合的默认属性。
     */
    private void initializeDefaults() {
        if (status == null) {
            status = UserStatus.DRAFT;
        }
        if (credential == null) {
            credential = PasswordCredential.empty();
        }
        if (roles == null) {
            roles = new ArrayList<>();
        }
    }

    /**
     * 在用户业务编码为空时通过网关生成业务编码。
     */
    private void generateNumIfBlank() {
        if (StrUtil.isBlank(getNum())) {
            setNum(userGateway.generateUserNum());
        }
    }

    /**
     * 当用户资料已填写时，校验并归一化用户资料。
     */
    private void validateProfileIfPresent() {
        boolean hasProfile = StrUtil.isNotBlank(email) || StrUtil.isNotBlank(phone)
                || StrUtil.isNotBlank(name) || CollUtil.isNotEmpty(roles) || StrUtil.isNotBlank(remark);
        if (!hasProfile) {
            return;
        }
        Assert.isTrue(UserStatus.DRAFT == status, "仅草稿态用户允许保存");
        validateProfile(email, phone, name, roles, remark);
        this.email = StrUtil.trim(email);
        this.phone = trimToNull(phone);
        this.name = StrUtil.trim(name);
        this.roles = new ArrayList<>(roles);
        this.remark = trimToNull(remark);
        userGateway.assertEmailPhoneUnique(this.email, this.phone, getNum());
    }

    /**
     * 校验用户资料入参。
     *
     * @param email 邮箱
     * @param phone 手机号
     * @param name 用户姓名
     * @param roles 平台角色列表
     * @param remark 备注
     */
    private void validateProfile(String email, String phone, String name, List<UserRole> roles, String remark) {
        Assert.notBlank(email, "请输入邮箱");
        Assert.isTrue(StrUtil.contains(email, "@"), "请输入正确的邮箱格式");
        if (StrUtil.isNotBlank(phone)) {
            Assert.isTrue(StrUtil.length(phone) >= 6, "请输入正确的手机号格式");
        }
        Assert.notBlank(name, "请输入姓名");
        if (StrUtil.isNotBlank(remark)) {
            Assert.isTrue(StrUtil.length(remark) <= 200, "备注不能超过 200 字");
        }
        validateRoles(roles);
    }

    /**
     * 去除字符串首尾空白，并将空白字符串转换为 null。
     *
     * @param value 待处理字符串
     * @return 处理后的字符串
     */
    private String trimToNull(String value) {
        String trimmed = StrUtil.trim(value);
        return StrUtil.isBlank(trimmed) ? null : trimmed;
    }

    /**
     * 校验平台角色列表。
     *
     * @param roles 平台角色列表
     */
    private void validateRoles(List<UserRole> roles) {
        Assert.isTrue(CollUtil.isNotEmpty(roles), "请至少选择一个角色");
        roles.forEach(UserRole::validate);
    }

    /**
     * 校验正式用户资料是否完整。
     */
    private void assertProfileComplete() {
        validateProfile(email, phone, name, roles, remark);
    }

    /**
     * 发布本次领域操作对应的单个领域事件。
     *
     * @param eventType 领域事件类型
     * @param operatorCode 当前操作人标识
     */
    private void publishEvent(String eventType, String operatorCode) {
        DomainEventDTO event = new DomainEventDTO();
        event.setEventId(IdUtil.fastSimpleUUID());
        event.setEventType(eventType);
        event.setBusinessNum(getNum());
        event.setOccurredAt(LocalDateTimeUtil.now());
        event.setOperatorCode(operatorCode);
        event.setPayload(this);
        this.domainEventPublisher.publish(event);
    }



    /**
     * 返回email。
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 返回phone。
     *
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 返回name。
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 返回roles。
     *
     * @return roles
     */
    public List<UserRole> getRoles() {
        return roles;
    }

    /**
     * 返回status。
     *
     * @return status
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * 返回credential。
     *
     * @return credential
     */
    public PasswordCredential getCredential() {
        return credential;
    }

    /**
     * 返回remark。
     *
     * @return remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置email。
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 设置phone。
     *
     * @param phone phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 设置name。
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置roles。
     *
     * @param roles roles
     */
    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    /**
     * 设置status。
     *
     * @param status status
     */
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * 设置credential。
     *
     * @param credential credential
     */
    public void setCredential(PasswordCredential credential) {
        this.credential = credential;
    }

    /**
     * 设置remark。
     *
     * @param remark remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 设置userRepository。
     *
     * @param userRepository userRepository
     */
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 设置userGateway。
     *
     * @param userGateway userGateway
     */
    public void setUserGateway(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    /**
     * 设置domainEventPublisher。
     *
     * @param domainEventPublisher domainEventPublisher
     */
    public void setDomainEventPublisher(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }
}