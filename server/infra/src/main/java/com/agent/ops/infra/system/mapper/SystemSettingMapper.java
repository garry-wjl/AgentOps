package com.agent.ops.infra.system.mapper;

import com.agent.ops.infra.system.entity.SystemSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置表 Mapper。
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSettingEntity> {
}
