package com.agent.ops.infra.model.mapper;

import com.agent.ops.infra.model.entity.ModelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型表 Mapper。
 */
@Mapper
public interface ModelMapper extends BaseMapper<ModelEntity> {
}
