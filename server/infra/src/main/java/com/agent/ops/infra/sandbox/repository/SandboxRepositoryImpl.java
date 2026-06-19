package com.agent.ops.infra.sandbox.repository;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.domain.sandbox.SandboxAggregate;
import com.agent.ops.domain.sandbox.repository.SandboxRepository;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.sandbox.entity.SandboxEntity;
import com.agent.ops.infra.sandbox.mapper.SandboxMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class SandboxRepositoryImpl implements SandboxRepository {
    @Resource
    private SandboxMapper sandboxMapper;

    @Override
    public void save(SandboxAggregate aggregate) {
        SandboxEntity entity = toEntity(aggregate);
        SandboxEntity existing = findEntityByNum(aggregate.getNum());
        if (existing == null) {
            sandboxMapper.insert(entity);
            aggregate.setId(entity.id);
            return;
        }
        entity.id = existing.id;
        entity.createNo = existing.createNo;
        entity.createTime = existing.createTime;
        sandboxMapper.updateById(entity);
        aggregate.setId(entity.id);
    }

    @Override
    public SandboxAggregate findByNum(String num) {
        SandboxEntity e = findEntityByNum(num);
        return e == null ? null : toDomain(e);
    }

    @Override
    public void deleteByNum(String num, String operatorCode) {
        LambdaUpdateWrapper<SandboxEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SandboxEntity::getNum, num)
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .set(SandboxEntity::getIsDeleted, InfraConstant.DELETED)
                .set(SandboxEntity::getUpdateNo, operatorCode);
        sandboxMapper.update(null, wrapper);
    }

    @Override
    public boolean existsByName(String spaceCode, String name, String excludeNum) {
        if (StrUtil.isBlank(name)) return false;
        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SandboxEntity::getSpaceCode, spaceCode)
                .eq(SandboxEntity::getName, name)
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SandboxEntity::getNum, excludeNum);
        }
        return sandboxMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<String> listProbeableNums() {
        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SandboxEntity::getStatus,
                        Arrays.asList(SandboxStatus.INITIALIZING.getCode(), SandboxStatus.ONLINE.getCode(),
                                SandboxStatus.OFFLINE.getCode()))
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        List<SandboxEntity> entities = sandboxMapper.selectList(wrapper);
        List<String> nums = new ArrayList<>();
        for (SandboxEntity e : entities) {
            nums.add(e.num);
        }
        return nums;
    }

    private SandboxEntity findEntityByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SandboxEntity::getNum, num)
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return sandboxMapper.selectOne(wrapper);
    }

    private SandboxEntity toEntity(SandboxAggregate a) {
        SandboxEntity e = new SandboxEntity();
        e.id = a.getId();
        e.num = a.getNum();
        e.spaceCode = a.getSpaceCode();
        e.name = a.getName();
        e.image = a.getImage();
        e.baseUrlOverride = a.getBaseUrlOverride();
        e.remark = a.getRemark();
        e.status = a.getStatus() == null ? SandboxStatus.DRAFT.getCode() : a.getStatus().getCode();
        e.lastStatusReason = a.getLastStatusReason();
        e.lastHeartbeatTime = a.getLastHeartbeatTime();
        e.createNo = a.getCreateNo();
        e.updateNo = a.getUpdateNo();
        e.createTime = a.getCreateTime();
        e.updateTime = a.getUpdateTime();
        e.isDeleted = InfraConstant.NOT_DELETED;
        return e;
    }

    private SandboxAggregate toDomain(SandboxEntity e) {
        SandboxAggregate a = new SandboxAggregate();
        a.setId(e.id);
        a.setNum(e.num);
        a.setSpaceCode(e.spaceCode);
        a.setName(e.name);
        a.setImage(e.image);
        a.setBaseUrlOverride(e.baseUrlOverride);
        a.setRemark(e.remark);
        a.setStatus(e.status == null ? null : SandboxStatus.fromCode(e.status));
        a.setLastStatusReason(e.lastStatusReason);
        a.setLastHeartbeatTime(e.lastHeartbeatTime);
        a.setCreateNo(e.createNo);
        a.setUpdateNo(e.updateNo);
        a.setCreateTime(e.createTime);
        a.setUpdateTime(e.updateTime);
        return a;
    }
}
