package com.agent.ops.application.model.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.model.dto.ModelDTO;
import com.agent.ops.client.model.enums.ModelStatus;
import com.agent.ops.client.model.param.ModelQueryParam;
import com.agent.ops.client.model.vo.ModelVO;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.model.entity.ModelEntity;
import com.agent.ops.infra.model.mapper.ModelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 模型读应用服务。
 */
@Service
public class ModelQueryService {
    @Resource
    private ModelMapper modelMapper;

    @Resource
    private SecretEncryptor secretEncryptor;

    /**
     * 按业务编码查询。
     *
     * @param num 业务编码
     * @return DTO，不存在返回 null
     */
    public ModelDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) {
            return null;
        }
        ModelEntity e = findEntityByNum(num);
        return e == null ? null : toDTO(e);
    }

    /**
     * 分页查询。
     *
     * @param param 入参
     * @return 分页结果
     */
    public PageResult<ModelVO> page(ModelQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getSpaceCode, param.spaceCode)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(ModelEntity::getName, param.keyword)
                    .or().like(ModelEntity::getModelId, param.keyword)
                    .or().like(ModelEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            ModelStatus s = ModelStatus.valueOf(param.status);
            wrapper.eq(ModelEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(ModelEntity::getUpdateTime);

        IPage<ModelEntity> page = new Page<>(pageNo, pageSize);
        IPage<ModelEntity> result = modelMapper.selectPage(page, wrapper);
        List<ModelVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (ModelEntity e : result.getRecords()) {
                records.add(toVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    /**
     * 查询启用态模型列表，供 Agent 装配选入对话框使用。
     *
     * @param spaceCode 空间业务编码
     * @return 模型 DTO 列表
     */
    public List<ModelDTO> getEnabledList(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getSpaceCode, spaceCode)
                .eq(ModelEntity::getStatus, ModelStatus.ENABLED.getCode())
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(ModelEntity::getUpdateTime);
        List<ModelEntity> entities = modelMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        List<ModelDTO> list = new ArrayList<>();
        for (ModelEntity e : entities) {
            list.add(toDTO(e));
        }
        return list;
    }

    private ModelEntity findEntityByNum(String num) {
        LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelEntity::getNum, num)
                .eq(ModelEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return modelMapper.selectOne(wrapper);
    }

    private ModelDTO toDTO(ModelEntity e) {
        ModelDTO dto = new ModelDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.modelId = e.modelId;
        dto.baseUrl = e.baseUrl;
        dto.apiKey = StrUtil.isBlank(e.apiKeyCipher) ? null : secretEncryptor.mask(e.apiKeyCipher);
        dto.remark = e.remark;
        dto.status = e.status == null ? null : ModelStatus.fromCode(e.status);
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private ModelVO toVO(ModelEntity e) {
        ModelVO vo = new ModelVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.modelId = e.modelId;
        vo.baseUrl = e.baseUrl;
        vo.apiKey = StrUtil.isBlank(e.apiKeyCipher) ? null : secretEncryptor.mask(e.apiKeyCipher);
        vo.remark = e.remark;
        vo.status = e.status == null ? null : ModelStatus.fromCode(e.status);
        vo.updateTime = e.updateTime;
        return vo;
    }
}
