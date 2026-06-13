package com.agent.ops.application.system.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.system.dto.AuditLogDTO;
import com.agent.ops.client.system.param.AuditLogQueryParam;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.system.entity.AuditLogEntity;
import com.agent.ops.infra.system.mapper.AuditLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 审计日志读应用服务。
 */
@Service
public class AuditLogQueryService {
    @Resource
    private AuditLogMapper auditLogMapper;

    /**
     * 分页查询审计日志。
     *
     * @param param 参数
     * @return 分页结果
     */
    public PageResult<AuditLogDTO> page(AuditLogQueryParam param) {
        Assert.notNull(param, "查询参数不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<AuditLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.module)) {
            wrapper.eq(AuditLogEntity::getModule, param.module);
        }
        if (StrUtil.isNotBlank(param.action)) {
            wrapper.eq(AuditLogEntity::getAction, param.action);
        }
        if (StrUtil.isNotBlank(param.operatorCodeFilter)) {
            wrapper.eq(AuditLogEntity::getOperatorCode, param.operatorCodeFilter);
        }
        if (StrUtil.isNotBlank(param.targetNum)) {
            wrapper.eq(AuditLogEntity::getTargetNum, param.targetNum);
        }
        if (param.fromTime != null) {
            wrapper.ge(AuditLogEntity::getCreateTime, param.fromTime);
        }
        if (param.toTime != null) {
            wrapper.le(AuditLogEntity::getCreateTime, param.toTime);
        }
        wrapper.orderByDesc(AuditLogEntity::getCreateTime);

        IPage<AuditLogEntity> page = new Page<>(pageNo, pageSize);
        IPage<AuditLogEntity> result = auditLogMapper.selectPage(page, wrapper);
        List<AuditLogDTO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (AuditLogEntity e : result.getRecords()) {
                records.add(toDTO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records.isEmpty() ? Collections.emptyList() : records);
    }

    /**
     * 实体 → DTO。
     *
     * @param e 实体
     * @return DTO
     */
    private AuditLogDTO toDTO(AuditLogEntity e) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.num = e.num;
        dto.module = e.module;
        dto.action = e.action;
        dto.operatorCode = e.operatorCode;
        dto.targetNum = e.targetNum;
        dto.detailJson = e.detailJson;
        dto.createTime = e.createTime;
        return dto;
    }
}
