package com.agent.ops.infra.space.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.space.enums.SpaceStatus;
import com.agent.ops.domain.space.SpaceAggregate;
import com.agent.ops.domain.space.repository.SpaceRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.space.entity.SpaceEntity;
import com.agent.ops.infra.space.mapper.SpaceMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间聚合仓储基础设施实现。
 */
@Repository
public class SpaceRepositoryImpl implements SpaceRepository {
    /**
     * 空间表 Mapper。
     */
    @Resource
    private SpaceMapper spaceMapper;

    /**
     * 持久化空间聚合（新建或更新）。
     *
     * @param space 空间聚合
     */
    @Override
    public void save(SpaceAggregate space) {
        SpaceEntity entity = toEntity(space);
        SpaceEntity existing = findEntityByNum(space.getNum());
        if (existing == null) {
            spaceMapper.insert(entity);
            space.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        spaceMapper.updateById(entity);
        space.setId(entity.id);
    }

    /**
     * 根据业务编码软删除空间。
     *
     * @param num          空间业务编码
     * @param operatorCode 当前操作人业务编码
     */
    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SpaceEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SpaceEntity::getNum, num)
                .eq(SpaceEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SpaceEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SpaceEntity::getUpdateNo, operatorCode);
        spaceMapper.update(null, wrapper);
    }

    /**
     * 根据业务编码加载空间聚合。
     *
     * @param num 空间业务编码
     * @return 空间聚合
     */
    @Override
    public SpaceAggregate findByNum(String num) {
        SpaceEntity entity = findEntityByNum(num);
        if (entity == null) {
            return null;
        }
        return toDomain(entity);
    }

    /**
     * 校验空间名称是否已存在（含软删除过滤）。
     *
     * @param name       空间名称
     * @param excludeNum 排除自检的业务编码
     * @return 是否已存在
     */
    @Override
    public boolean existsByName(String name, String excludeNum) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        LambdaQueryWrapper<SpaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceEntity::getName, name)
                .eq(SpaceEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SpaceEntity::getNum, excludeNum);
        }
        return spaceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 按业务编码查询未删除空间实体。
     *
     * @param num 空间业务编码
     * @return 空间实体
     */
    private SpaceEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        LambdaQueryWrapper<SpaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceEntity::getNum, num)
                .eq(SpaceEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return spaceMapper.selectOne(wrapper);
    }

    /**
     * 将领域聚合转换为持久化对象。
     *
     * @param space 空间聚合
     * @return 空间持久化对象
     */
    private SpaceEntity toEntity(SpaceAggregate space) {
        SpaceEntity entity = new SpaceEntity();
        entity.id = space.getId();
        entity.num = space.getNum();
        entity.name = space.getName();
        entity.description = space.getDescription();
        entity.iconUrl = space.getIconUrl();
        entity.ownerUserCode = space.getOwnerUserCode();
        entity.status = space.getStatus() == null ? SpaceStatus.ENABLED.getCode() : space.getStatus().getCode();
        entity.adminUserCodes = JSON.toJSONString(space.getAdminUserCodes() == null ? Collections.emptyList() : space.getAdminUserCodes());
        entity.memberUserCodes = JSON.toJSONString(space.getMemberUserCodes() == null ? Collections.emptyList() : space.getMemberUserCodes());
        entity.createNo = space.getCreateNo();
        entity.updateNo = space.getUpdateNo();
        entity.createTime = space.getCreateTime();
        entity.updateTime = space.getUpdateTime();
        entity.isDeleted = InfraConstant.NOT_DELETED;
        return entity;
    }

    /**
     * 将持久化对象转换为领域聚合。
     *
     * @param entity 空间持久化对象
     * @return 空间聚合
     */
    private SpaceAggregate toDomain(SpaceEntity entity) {
        SpaceAggregate space = new SpaceAggregate();
        space.setId(entity.id);
        space.setNum(entity.num);
        space.setName(entity.name);
        space.setDescription(entity.description);
        space.setIconUrl(entity.iconUrl);
        space.setOwnerUserCode(entity.ownerUserCode);
        space.setStatus(entity.status == null ? null : SpaceStatus.fromCode(entity.status));
        space.setAdminUserCodes(parseCodeList(entity.adminUserCodes));
        space.setMemberUserCodes(parseCodeList(entity.memberUserCodes));
        space.setCreateNo(entity.createNo);
        space.setUpdateNo(entity.updateNo);
        space.setCreateTime(entity.createTime);
        space.setUpdateTime(entity.updateTime);
        return space;
    }

    /**
     * 解析 JSON 字符串数组为字符串列表。
     *
     * @param json JSON 字符串
     * @return 字符串列表
     */
    private List<String> parseCodeList(String json) {
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }
}
