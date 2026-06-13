package com.agent.ops.application.prompt.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.prompt.dto.PromptDTO;
import com.agent.ops.client.prompt.enums.PromptStatus;
import com.agent.ops.client.prompt.param.PromptQueryParam;
import com.agent.ops.client.prompt.vo.PromptVO;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.prompt.entity.PromptEntity;
import com.agent.ops.infra.prompt.mapper.PromptMapper;
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
public class PromptQueryService {
    @Resource
    private PromptMapper promptMapper;

    public PromptDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        PromptEntity e = findEntityByNum(num);
        return e == null ? null : toDTO(e);
    }

    public PromptDTO getEnabledByKey(String spaceCode, String key) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(key)) {
            return null;
        }
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, spaceCode)
                .eq(PromptEntity::getKey, key)
                .eq(PromptEntity::getStatus, PromptStatus.ENABLED.getCode())
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        PromptEntity e = promptMapper.selectOne(wrapper);
        return e == null ? null : toDTO(e);
    }

    public List<PromptDTO> getEnabledList(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, spaceCode)
                .eq(PromptEntity::getStatus, PromptStatus.ENABLED.getCode())
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(PromptEntity::getUpdateTime);
        List<PromptEntity> entities = promptMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<PromptDTO> list = new ArrayList<>();
        for (PromptEntity e : entities) {
            list.add(toDTO(e));
        }
        return list;
    }

    public PageResult<PromptVO> page(PromptQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getSpaceCode, param.spaceCode)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(PromptEntity::getName, param.keyword)
                    .or().like(PromptEntity::getKey, param.keyword)
                    .or().like(PromptEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            PromptStatus s = PromptStatus.valueOf(param.status);
            wrapper.eq(PromptEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(PromptEntity::getUpdateTime);

        IPage<PromptEntity> page = new Page<>(pageNo, pageSize);
        IPage<PromptEntity> result = promptMapper.selectPage(page, wrapper);
        List<PromptVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (PromptEntity e : result.getRecords()) {
                records.add(toVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    private PromptEntity findEntityByNum(String num) {
        LambdaQueryWrapper<PromptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptEntity::getNum, num)
                .eq(PromptEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return promptMapper.selectOne(wrapper);
    }

    private PromptDTO toDTO(PromptEntity e) {
        PromptDTO dto = new PromptDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.key = e.key;
        dto.content = e.content;
        dto.variables = parseVariables(e.variablesJson);
        dto.remark = e.remark;
        dto.status = e.status == null ? null : PromptStatus.fromCode(e.status);
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private PromptVO toVO(PromptEntity e) {
        PromptVO vo = new PromptVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.key = e.key;
        vo.contentPreview = e.content == null ? null : (e.content.length() > 100 ? e.content.substring(0, 100) : e.content);
        vo.variables = parseVariables(e.variablesJson);
        vo.remark = e.remark;
        vo.status = e.status == null ? null : PromptStatus.fromCode(e.status);
        vo.updateTime = e.updateTime;
        return vo;
    }

    private List<String> parseVariables(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : list;
    }
}
