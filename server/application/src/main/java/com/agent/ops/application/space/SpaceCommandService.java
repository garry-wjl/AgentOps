package com.agent.ops.application.space;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.user.UserQueryService;
import com.agent.ops.client.space.dto.SpaceDTO;
import com.agent.ops.client.space.enums.SpaceRoleType;
import com.agent.ops.client.space.enums.SpaceStatus;
import com.agent.ops.client.space.param.ChangeMemberRoleParam;
import com.agent.ops.client.space.param.CreateSpaceParam;
import com.agent.ops.client.space.param.DeleteSpaceParam;
import com.agent.ops.client.space.param.JoinMemberParam;
import com.agent.ops.client.space.param.RemoveMemberParam;
import com.agent.ops.client.space.param.UpdateSpaceParam;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.domain.space.SpaceAggregate;
import com.agent.ops.domain.space.factory.SpaceFactory;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 空间写用例应用服务。
 * <p>
 * 包含主体 CRUD（create/updateBasic/delete）以及成员管理（addMember/removeMember/changeMemberRole）。
 * 改字段类操作通过 setter + save 完成；成员相关事件由本类显式发布。
 */
@Service
public class SpaceCommandService {
    /**
     * 空间聚合工厂。
     */
    @Resource
    private SpaceFactory spaceFactory;

    /**
     * 空间查询应用服务（读模型协作）。
     */
    @Resource
    private SpaceQueryService spaceQueryService;

    /**
     * 用户查询应用服务（跨领域）。
     */
    @Resource
    private UserQueryService userQueryService;

    /**
     * Redis 分布式锁。
     */
    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 创建空间。
     *
     * @param param 创建空间入参
     * @return 空间 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpaceDTO create(CreateSpaceParam param) {
        Assert.notNull(param, "创建空间参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "当前用户业务编码不能为空");
        Assert.notBlank(param.name, "空间名称不能为空");
        String operatorCode = param.getOperatorCode();
        String lockKey = "space:name:" + param.name;
        return distributedLock.execute(lockKey, () -> {
            if (spaceQueryService.existsByName(param.name, null)) {
                throw new BusinessException("SPACE_NAME_DUPLICATED", "空间名称已存在");
            }
            SpaceAggregate space = spaceFactory.create(param.name, param.description, param.iconUrl, operatorCode);
            space.save(operatorCode);
            return toDTO(space);
        });
    }

    /**
     * 修改空间基础信息（改字段：setter + save）。
     *
     * @param param 修改空间入参
     * @return 空间 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpaceDTO updateBasic(UpdateSpaceParam param) {
        Assert.notNull(param, "修改空间参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        String operatorCode = param.getOperatorCode();
        Assert.notBlank(operatorCode, "当前用户业务编码不能为空");
        String lockKey = "space:" + param.spaceCode;
        return distributedLock.execute(lockKey, () -> {
            SpaceAggregate space = loadSpace(param.spaceCode);
            assertAdminOrOwner(space, operatorCode);
            if (StrUtil.isNotBlank(param.name)) {
                space.setName(param.name);
            }
            if (param.description != null) {
                space.setDescription(param.description);
            }
            if (param.iconUrl != null) {
                space.setIconUrl(param.iconUrl);
            }
            space.save(operatorCode);
            return toDTO(space);
        });
    }

    /**
     * 软删除空间（确认输入式）。
     *
     * @param param 删除空间入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteSpaceParam param) {
        Assert.notNull(param, "删除空间参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        Assert.notBlank(param.confirmName, "请输入空间名称以确认删除");
        String operatorCode = param.getOperatorCode();
        Assert.notBlank(operatorCode, "当前用户业务编码不能为空");
        String lockKey = "space:" + param.spaceCode;
        distributedLock.run(lockKey, () -> {
            SpaceAggregate space = loadSpace(param.spaceCode);
            if (!StrUtil.equals(space.getName(), param.confirmName)) {
                throw new BusinessException("SPACE_DELETE_NAME_MISMATCH", "确认输入的空间名称与目标空间不一致");
            }
            space.delete(operatorCode);
        });
    }

    /**
     * 加入空间成员。
     *
     * @param param 加入成员入参
     * @return 空间 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpaceDTO addMember(JoinMemberParam param) {
        Assert.notNull(param, "加入成员参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        Assert.notBlank(param.userCode, "用户业务编码不能为空");
        Assert.notNull(param.roleType, "角色类型不能为空");
        String operatorCode = param.getOperatorCode();
        Assert.notBlank(operatorCode, "当前用户业务编码不能为空");

        // 跨领域校验目标用户存在。
        UserDTO target = userQueryService.getByNum(param.userCode);
        if (target == null) {
            throw new BusinessException("USER_NOT_FOUND", "目标用户不存在");
        }
        String lockKey = "space:" + param.spaceCode;
        return distributedLock.execute(lockKey, () -> {
            SpaceAggregate space = loadSpace(param.spaceCode);
            assertAdminOrOwner(space, operatorCode);
            ensureMutableLists(space);
            String userCode = param.userCode;
            // 校验未在 admin / member 列表中。
            if (space.getAdminUserCodes().contains(userCode) || space.getMemberUserCodes().contains(userCode)) {
                throw new BusinessException("MEMBER_ALREADY_EXISTS", "用户已是该空间成员");
            }
            if (param.roleType == SpaceRoleType.ADMIN) {
                space.getAdminUserCodes().add(userCode);
            } else {
                space.getMemberUserCodes().add(userCode);
            }
            space.save(operatorCode);
            space.publishMemberAdded(userCode, param.roleType.name(), operatorCode);
            return toDTO(space);
        });
    }

    /**
     * 移除空间成员。
     *
     * @param param 移除成员入参
     * @return 空间 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpaceDTO removeMember(RemoveMemberParam param) {
        Assert.notNull(param, "移除成员参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        Assert.notBlank(param.userCode, "用户业务编码不能为空");
        String operatorCode = param.getOperatorCode();
        Assert.notBlank(operatorCode, "当前用户业务编码不能为空");
        String lockKey = "space:" + param.spaceCode;
        return distributedLock.execute(lockKey, () -> {
            SpaceAggregate space = loadSpace(param.spaceCode);
            assertAdminOrOwner(space, operatorCode);
            ensureMutableLists(space);
            String userCode = param.userCode;
            if (StrUtil.equals(userCode, space.getOwnerUserCode())) {
                throw new BusinessException("CANNOT_REMOVE_OWNER", "不能移除空间所有者");
            }
            boolean removed = space.getAdminUserCodes().remove(userCode);
            if (!removed) {
                removed = space.getMemberUserCodes().remove(userCode);
            }
            if (!removed) {
                throw new BusinessException("MEMBER_NOT_FOUND", "用户不在该空间成员中");
            }
            space.save(operatorCode);
            space.publishMemberRemoved(userCode, operatorCode);
            return toDTO(space);
        });
    }

    /**
     * 修改成员角色（在管理员与普通成员之间迁移）。
     *
     * @param param 修改成员角色入参
     * @return 空间 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpaceDTO changeMemberRole(ChangeMemberRoleParam param) {
        Assert.notNull(param, "修改成员角色参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        Assert.notBlank(param.userCode, "用户业务编码不能为空");
        Assert.notNull(param.roleType, "角色类型不能为空");
        String operatorCode = param.getOperatorCode();
        Assert.notBlank(operatorCode, "当前用户业务编码不能为空");
        String lockKey = "space:" + param.spaceCode;
        return distributedLock.execute(lockKey, () -> {
            SpaceAggregate space = loadSpace(param.spaceCode);
            assertAdminOrOwner(space, operatorCode);
            ensureMutableLists(space);
            String userCode = param.userCode;
            if (StrUtil.equals(userCode, space.getOwnerUserCode())) {
                throw new BusinessException("CANNOT_CHANGE_OWNER_ROLE", "不能修改空间所有者的角色");
            }
            boolean inAdmin = space.getAdminUserCodes().contains(userCode);
            boolean inMember = space.getMemberUserCodes().contains(userCode);
            if (!inAdmin && !inMember) {
                throw new BusinessException("MEMBER_NOT_FOUND", "用户不在该空间成员中");
            }
            if (param.roleType == SpaceRoleType.ADMIN) {
                if (inAdmin) {
                    return toDTO(space);
                }
                space.getMemberUserCodes().remove(userCode);
                space.getAdminUserCodes().add(userCode);
            } else {
                if (inMember) {
                    return toDTO(space);
                }
                space.getAdminUserCodes().remove(userCode);
                space.getMemberUserCodes().add(userCode);
            }
            space.save(operatorCode);
            space.publishMemberRoleChanged(userCode, param.roleType.name(), operatorCode);
            return toDTO(space);
        });
    }

    /**
     * 校验当前用户是空间管理员或所有者，否则抛权限异常。
     *
     * @param space          空间聚合
     * @param operatorCode   当前操作人业务编码
     */
    private void assertAdminOrOwner(SpaceAggregate space, String operatorCode) {
        if (StrUtil.equals(operatorCode, space.getOwnerUserCode())) {
            return;
        }
        if (space.getAdminUserCodes() != null && space.getAdminUserCodes().contains(operatorCode)) {
            return;
        }
        throw new BusinessException("ACCESS_DENIED", "当前用户无权管理该空间");
    }

    /**
     * 确保聚合内的列表可变（避免聚合持有的 list 被仓储反序列化为不可变集合）。
     *
     * @param space 空间聚合
     */
    private void ensureMutableLists(SpaceAggregate space) {
        if (!(space.getAdminUserCodes() instanceof ArrayList<String>)) {
            space.setAdminUserCodes(new ArrayList<>(space.getAdminUserCodes() == null ? List.of() : space.getAdminUserCodes()));
        }
        if (!(space.getMemberUserCodes() instanceof ArrayList<String>)) {
            space.setMemberUserCodes(new ArrayList<>(space.getMemberUserCodes() == null ? List.of() : space.getMemberUserCodes()));
        }
    }

    /**
     * 加载空间聚合。
     *
     * @param spaceCode 空间业务编码
     * @return 空间聚合
     */
    private SpaceAggregate loadSpace(String spaceCode) {
        SpaceAggregate space = spaceFactory.createByNum(spaceCode);
        if (space == null) {
            throw new BusinessException("SPACE_NOT_FOUND", "空间不存在");
        }
        return space;
    }

    /**
     * 把空间聚合转换为 DTO。
     *
     * @param space 空间聚合
     * @return 空间 DTO
     */
    private SpaceDTO toDTO(SpaceAggregate space) {
        SpaceDTO dto = new SpaceDTO();
        dto.num = space.getNum();
        dto.name = space.getName();
        dto.description = space.getDescription();
        dto.iconUrl = space.getIconUrl();
        dto.ownerUserCode = space.getOwnerUserCode();
        dto.status = space.getStatus() == null ? SpaceStatus.ENABLED : space.getStatus();
        dto.adminUserCodes = space.getAdminUserCodes() == null ? new ArrayList<>() : new ArrayList<>(space.getAdminUserCodes());
        dto.memberUserCodes = space.getMemberUserCodes() == null ? new ArrayList<>() : new ArrayList<>(space.getMemberUserCodes());
        dto.createTime = space.getCreateTime();
        dto.updateTime = space.getUpdateTime();
        return dto;
    }
}
