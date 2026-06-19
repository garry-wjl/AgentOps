package com.agent.ops.application.skill.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.dto.SkillDTO;
import com.agent.ops.client.skill.dto.SkillResourceFileDTO;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.client.skill.enums.SkillStatus;
import com.agent.ops.client.skill.enums.SkillVersionStatus;
import com.agent.ops.client.skill.param.SkillQueryParam;
import com.agent.ops.client.skill.vo.SkillVO;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.skill.entity.SkillEntity;
import com.agent.ops.infra.skill.entity.SkillResourceFileEntity;
import com.agent.ops.infra.skill.entity.SkillVersionEntity;
import com.agent.ops.infra.skill.mapper.SkillMapper;
import com.agent.ops.infra.skill.mapper.SkillResourceFileMapper;
import com.agent.ops.infra.skill.mapper.SkillVersionMapper;
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
public class SkillQueryService {
    @Resource
    private SkillMapper skillMapper;

    @Resource
    private SkillVersionMapper skillVersionMapper;

    @Resource
    private SkillResourceFileMapper skillResourceFileMapper;

    public SkillDTO getByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        SkillEntity e = findSkillByNum(num);
        return e == null ? null : toSkillDTO(e);
    }

    public PageResult<SkillVO> page(SkillQueryParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getSpaceCode, param.spaceCode)
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(param.keyword)) {
            wrapper.and(q -> q.like(SkillEntity::getName, param.keyword)
                    .or().like(SkillEntity::getNum, param.keyword));
        }
        if (StrUtil.isNotBlank(param.status)) {
            SkillStatus s = SkillStatus.valueOf(param.status);
            wrapper.eq(SkillEntity::getStatus, s.getCode());
        }
        wrapper.orderByDesc(SkillEntity::getUpdateTime);

        IPage<SkillEntity> page = new Page<>(pageNo, pageSize);
        IPage<SkillEntity> result = skillMapper.selectPage(page, wrapper);
        List<SkillVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (SkillEntity e : result.getRecords()) {
                records.add(toSkillVO(e));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    /**
     * 列出空间内可被 Agent 引用的 Skill：主体生效 + 至少一个生效版本。
     *
     * @param spaceCode 空间业务编码
     * @return Skill DTO 列表
     */
    public List<SkillDTO> getEffectiveListForReference(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) return Collections.emptyList();
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getSpaceCode, spaceCode)
                .eq(SkillEntity::getStatus, SkillStatus.EFFECTIVE.getCode())
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        List<SkillEntity> entities = skillMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<SkillDTO> list = new ArrayList<>();
        for (SkillEntity e : entities) {
            // 校验该 Skill 至少有一个生效版本
            LambdaQueryWrapper<SkillVersionEntity> vw = new LambdaQueryWrapper<>();
            vw.eq(SkillVersionEntity::getSkillCode, e.num)
                    .eq(SkillVersionEntity::getStatus, SkillVersionStatus.EFFECTIVE.getCode())
                    .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED);
            if (skillVersionMapper.selectCount(vw) > 0) {
                list.add(toSkillDTO(e));
            }
        }
        return list;
    }

    public List<SkillVersionDTO> listVersionsBySkillCode(String skillCode) {
        if (StrUtil.isBlank(skillCode)) return Collections.emptyList();
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByDesc(SkillVersionEntity::getCreateTime);
        List<SkillVersionEntity> entities = skillVersionMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<SkillVersionDTO> list = new ArrayList<>();
        for (SkillVersionEntity e : entities) {
            list.add(toVersionDTO(e));
        }
        return list;
    }

    public SkillVersionDTO getVersionByNum(String num) {
        if (StrUtil.isBlank(num)) return null;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getNum, num)
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        SkillVersionEntity e = skillVersionMapper.selectOne(wrapper);
        return e == null ? null : toVersionDTO(e);
    }

    public SkillVersionDTO getEffectiveVersionBySkillCode(String skillCode) {
        if (StrUtil.isBlank(skillCode)) return null;
        LambdaQueryWrapper<SkillVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillVersionEntity::getSkillCode, skillCode)
                .eq(SkillVersionEntity::getStatus, SkillVersionStatus.EFFECTIVE.getCode())
                .eq(SkillVersionEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        SkillVersionEntity e = skillVersionMapper.selectOne(wrapper);
        return e == null ? null : toVersionDTO(e);
    }

    public List<SkillResourceFileDTO> listResourceFiles(String versionCode) {
        if (StrUtil.isBlank(versionCode)) return Collections.emptyList();
        LambdaQueryWrapper<SkillResourceFileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillResourceFileEntity::getSkillVersionCode, versionCode)
                .eq(SkillResourceFileEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .orderByAsc(SkillResourceFileEntity::getPath);
        List<SkillResourceFileEntity> entities = skillResourceFileMapper.selectList(wrapper);
        if (CollUtil.isEmpty(entities)) return Collections.emptyList();
        List<SkillResourceFileDTO> list = new ArrayList<>();
        for (SkillResourceFileEntity e : entities) {
            list.add(toResourceDTO(e));
        }
        return list;
    }

    private SkillEntity findSkillByNum(String num) {
        LambdaQueryWrapper<SkillEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillEntity::getNum, num)
                .eq(SkillEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return skillMapper.selectOne(wrapper);
    }

    private SkillDTO toSkillDTO(SkillEntity e) {
        SkillDTO dto = new SkillDTO();
        dto.num = e.num;
        dto.spaceCode = e.spaceCode;
        dto.name = e.name;
        dto.description = e.description;
        dto.currentVersionNo = e.currentVersionNo;
        dto.status = e.status == null ? null : SkillStatus.fromCode(e.status);
        dto.tags = parseTags(e.tagsJson);
        dto.remark = e.remark;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private SkillVO toSkillVO(SkillEntity e) {
        SkillVO vo = new SkillVO();
        vo.num = e.num;
        vo.name = e.name;
        vo.description = e.description;
        vo.currentVersionNo = e.currentVersionNo;
        vo.status = e.status == null ? null : SkillStatus.fromCode(e.status);
        vo.tags = parseTags(e.tagsJson);
        vo.updateTime = e.updateTime;
        return vo;
    }

    private SkillVersionDTO toVersionDTO(SkillVersionEntity e) {
        SkillVersionDTO dto = new SkillVersionDTO();
        dto.num = e.num;
        dto.skillCode = e.skillCode;
        dto.versionNo = e.versionNo;
        dto.skillMdContent = e.skillMdContent;
        dto.status = e.status == null ? null : SkillVersionStatus.fromCode(e.status);
        dto.publishTime = e.publishTime;
        dto.withdrawTime = e.withdrawTime;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private SkillResourceFileDTO toResourceDTO(SkillResourceFileEntity e) {
        SkillResourceFileDTO dto = new SkillResourceFileDTO();
        dto.num = e.num;
        dto.skillVersionCode = e.skillVersionCode;
        dto.path = e.path;
        dto.type = e.type == null ? null : FileType.fromCode(e.type);
        dto.content = e.content;
        dto.sizeBytes = e.sizeBytes;
        dto.createTime = e.createTime;
        dto.updateTime = e.updateTime;
        return dto;
    }

    private List<String> parseTags(String json) {
        if (StrUtil.isBlank(json)) return new ArrayList<>();
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? new ArrayList<>() : list;
    }
}
