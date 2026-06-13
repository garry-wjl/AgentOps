package com.agent.ops.infra.space.mapper;

import com.agent.ops.infra.space.entity.SpaceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 空间表 Mapper。
 */
@Mapper
public interface SpaceMapper extends BaseMapper<SpaceEntity> {
    /**
     * 分页查询包含指定用户业务编码（在 admin 或 member 列表中）的空间。
     *
     * @param page     MyBatis-Plus 分页对象
     * @param userCode 用户业务编码
     * @param keyword  名称关键字（可选）
     * @return 分页结果
     */
    IPage<SpaceEntity> pageContaining(@Param("page") IPage<SpaceEntity> page,
                                      @Param("userCode") String userCode,
                                      @Param("keyword") String keyword);
}
