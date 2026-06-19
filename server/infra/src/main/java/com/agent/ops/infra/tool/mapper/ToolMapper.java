package com.agent.ops.infra.tool.mapper;

import com.agent.ops.infra.tool.entity.ToolEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolMapper extends BaseMapper<ToolEntity> {
}
