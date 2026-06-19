package com.agent.ops.application.sandbox.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.sandbox.dto.SandboxDTO;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.client.sandbox.param.SandboxQueryParam;
import com.agent.ops.client.sandbox.vo.SandboxVO;
import com.agent.ops.domain.sandbox.repository.SandboxRepository;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.sandbox.entity.SandboxEntity;
import com.agent.ops.infra.sandbox.mapper.SandboxMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SandboxQueryService {
    @Resource
    private SandboxMapper sandboxMapper;

    /**
     * 通过仓储取可探活列表（供 Scheduler 调用）。
     */
    @Resource
    private SandboxRepository sandboxRepository;

    public SandboxDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        SandboxEntity e = findEntityByNum(num);
        return e == null ? null : toDTO(e);
    }

    public PageResult<SandboxVO> page(SandboxQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SandboxEntity::getSpaceCode, param.spaceCode)
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(SandboxEntity::getName, param.keyword)
                    .or().like(SandboxEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            SandboxStatus s = SandboxStatus.valueOf(param.status);
            wrapper.eq(SandboxEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(SandboxEntity::getUpdateTime);

        IPage<SandboxEntity> page = new Page<>(pageNo, pageSize);
        IPage<SandboxEntity> result = sandboxMapper.selectPage(page, wrapper);
        List<SandboxVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (SandboxEntity e : result.getRecords()) {
                records.add(toVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    /**
     * 列出指定空间内可被 Agent 引用的沙箱（非 DISABLED 且非 DRAFT）。
     *
     * @param spaceCode 空间业务编码
     * @return 沙箱 DTO 列表
     */
    public List<SandboxDTO> getAvailableList(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SandboxEntity::getSpaceCode, spaceCode)
                .in(SandboxEntity::getStatus, Arrays.asList(SandboxStatus.INITIALIZING.getCode(),
                        SandboxStatus.ONLINE.getCode(), SandboxStatus.OFFLINE.getCode()))
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(SandboxEntity::getUpdateTime);
        List<SandboxEntity> entities = sandboxMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<SandboxDTO> list = new ArrayList<>();
        for (SandboxEntity e : entities) {
            list.add(toDTO(e));
        }
        return list;
    }

    /**
     * 列出可探活的业务编码（供 Scheduler 调用）。
     *
     * @return 业务编码列表
     */
    public List<String> listProbeable() {
        return sandboxRepository.listProbeableNums();
    }

    private SandboxEntity findEntityByNum(String num) {
        LambdaQueryWrapper<SandboxEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SandboxEntity::getNum, num)
                .eq(SandboxEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return sandboxMapper.selectOne(wrapper);
    }

    private SandboxDTO toDTO(SandboxEntity e) {
        SandboxDTO dto = new SandboxDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.image = e.image;
        dto.baseUrlOverride = e.baseUrlOverride;
        dto.remark = e.remark;
        dto.status = e.status == null ? null : SandboxStatus.fromCode(e.status);
        dto.lastStatusReason = e.lastStatusReason;
        dto.lastHeartbeatTime = e.lastHeartbeatTime;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private SandboxVO toVO(SandboxEntity e) {
        SandboxVO vo = new SandboxVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.image = e.image;
        vo.baseUrlOverride = e.baseUrlOverride;
        vo.remark = e.remark;
        vo.status = e.status == null ? null : SandboxStatus.fromCode(e.status);
        vo.lastStatusReason = e.lastStatusReason;
        vo.lastHeartbeatTime = e.lastHeartbeatTime;
        vo.updateTime = e.updateTime;
        return vo;
    }
}
