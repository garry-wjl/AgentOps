package com.agent.ops.application.tool.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.tool.dto.ToolDTO;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.client.tool.param.ToolQueryParam;
import com.agent.ops.client.tool.vo.ToolVO;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.tool.entity.ToolEntity;
import com.agent.ops.infra.tool.mapper.ToolMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ToolQueryService {
    @Resource
    private ToolMapper toolMapper;

    public ToolDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        ToolEntity e = findEntityByNum(num);
        return e == null ? null : toDTO(e);
    }

    public PageResult<ToolVO> page(ToolQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getSpaceCode, param.spaceCode)
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(ToolEntity::getName, param.keyword)
                    .or().like(ToolEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.type)) {
            wrapper.eq(ToolEntity::getType, param.type);
        }
        if (StrUtil.isNotBlank(param.subType)) {
            wrapper.eq(ToolEntity::getSubType, param.subType);
        }
        if (StrUtil.isNotBlank(param.status)) {
            ToolStatus s = ToolStatus.valueOf(param.status);
            wrapper.eq(ToolEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(ToolEntity::getUpdateTime);

        IPage<ToolEntity> page = new Page<>(pageNo, pageSize);
        IPage<ToolEntity> result = toolMapper.selectPage(page, wrapper);
        List<ToolVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (ToolEntity e : result.getRecords()) {
                records.add(toVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    public List<ToolDTO> getEffectiveList(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getSpaceCode, spaceCode)
                .eq(ToolEntity::getStatus, ToolStatus.EFFECTIVE.getCode())
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(ToolEntity::getUpdateTime);
        List<ToolEntity> entities = toolMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<ToolDTO> list = new ArrayList<>();
        for (ToolEntity e : entities) {
            list.add(toDTO(e));
        }
        return list;
    }

    private ToolEntity findEntityByNum(String num) {
        LambdaQueryWrapper<ToolEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ToolEntity::getNum, num)
                .eq(ToolEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return toolMapper.selectOne(wrapper);
    }

    private ToolDTO toDTO(ToolEntity e) {
        ToolDTO dto = new ToolDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.type = e.type == null ? null : ToolType.valueOf(e.type);
        dto.subType = e.subType == null ? null : ToolSubType.valueOf(e.subType);
        dto.description = e.description;
        dto.tags = parseTags(e.tagsJson);
        dto.configJson = e.configJson;
        dto.status = e.status == null ? null : ToolStatus.fromCode(e.status);
        dto.remark = e.remark;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private ToolVO toVO(ToolEntity e) {
        ToolVO vo = new ToolVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.type = e.type == null ? null : ToolType.valueOf(e.type);
        vo.subType = e.subType == null ? null : ToolSubType.valueOf(e.subType);
        vo.description = e.description;
        vo.tags = parseTags(e.tagsJson);
        vo.status = e.status == null ? null : ToolStatus.fromCode(e.status);
        vo.remark = e.remark;
        vo.updateTime = e.updateTime;
        return vo;
    }

    private List<String> parseTags(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : list;
    }
}
