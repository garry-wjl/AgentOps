package com.agent.ops.infra.sandbox.mapper;

import com.agent.ops.infra.sandbox.entity.SandboxEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SandboxMapper extends BaseMapper<SandboxEntity> {
}
