package com.agent.ops.infra.agent.mapper;

import com.agent.ops.infra.agent.entity.AgentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMapper extends BaseMapper<AgentEntity> {
}
