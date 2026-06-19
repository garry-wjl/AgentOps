package com.agent.ops.infra.prompt.mapper;

import com.agent.ops.infra.prompt.entity.PromptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptMapper extends BaseMapper<PromptEntity> {
}
