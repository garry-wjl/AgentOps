package com.agent.ops.application.space;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.user.UserQueryService;
import com.agent.ops.client.space.dto.SpaceDTO;
import com.agent.ops.client.space.enums.SpaceRoleType;
import com.agent.ops.client.space.enums.SpaceStatus;
import com.agent.ops.client.space.param.SpaceMembersQueryParam;
import com.agent.ops.client.space.param.SpaceQueryParam;
import com.agent.ops.client.space.vo.SpaceCardVO;
import com.agent.ops.client.space.vo.SpaceMemberVO;
import com.agent.ops.client.user.dto.UserDTO;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.constant.InfraConstant;
import com.agent.ops.infra.space.entity.SpaceEntity;
import com.agent.ops.infra.space.mapper.SpaceMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 空间读用例应用服务。
 */
@Service
public class SpaceQueryService {
    /**
     * 空间表 Mapper（仅用于只读查询）。
     */
    @Resource
    private SpaceMapper spaceMapper;

    /**
     * 用户查询应用服务。
     */
    @Resource
    private UserQueryService userQueryService;

    /**
     * 根据业务编码查询空间数据传输对象。
     *
     * @param spaceCode 空间业务编码
     * @return 空间数据传输对象，不存在时抛业务异常
     */
    public SpaceDTO getByCode(String spaceCode) {
        Assert.notBlank(spaceCode, "空间业务编码不能为空");
        SpaceEntity entity = findEntityByCode(spaceCode);
        Assert.notNull(entity, "空间不存在");
        return toSpaceDTO(entity);
    }

    /**
     * 根据业务编码查询空间数据传输对象，不存在返回 null（不抛异常）。
     *
     * @param spaceCode 空间业务编码
     * @return 空间数据传输对象
     */
    public SpaceDTO findByCode(String spaceCode) {
        if (StrUtil.isBlank(spaceCode)) {
            return null;
        }
        SpaceEntity entity = findEntityByCode(spaceCode);
        if (entity == null) {
            return null;
        }
        return toSpaceDTO(entity);
    }

    /**
     * 校验空间名称是否已存在（含软删除过滤）。
     *
     * @param name       空间名称
     * @param excludeNum 排除自检的空间业务编码，null 表示不排除
     * @return 是否已存在
     */
    public boolean existsByName(String name, String excludeNum) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        LambdaQueryWrapper<SpaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceEntity::getName, name)
                .eq(SpaceEntity::getIsDeleted, InfraConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(excludeNum)) {
            wrapper.ne(SpaceEntity::getNum, excludeNum);
        }
        return spaceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 分页查询当前用户所属的空间卡片列表。
     *
     * @param param 分页查询参数（必须包含 currentUserCode）
     * @return 分页结果
     */
    public PageResult<SpaceCardVO> pageMine(SpaceQueryParam param) {
        Assert.notNull(param, "空间分页查询参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "当前用户业务编码不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        IPage<SpaceEntity> page = new Page<>(pageNo, pageSize);
        IPage<SpaceEntity> result = spaceMapper.pageContaining(page, param.getOperatorCode(), param.keyword);
        List<SpaceCardVO> records = new ArrayList<>();
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (SpaceEntity entity : result.getRecords()) {
                records.add(toSpaceCardVO(entity, param.getOperatorCode()));
            }
        }
        long total = result == null ? 0L : result.getTotal();
        return PageResult.of(total, pageNo, pageSize, records);
    }

    /**
     * 分页查询空间成员列表。
     *
     * @param param 成员分页查询参数
     * @return 分页结果
     */
    public PageResult<SpaceMemberVO> pageMembers(SpaceMembersQueryParam param) {
        Assert.notNull(param, "空间成员分页查询参数不能为空");
        Assert.notBlank(param.spaceCode, "空间业务编码不能为空");
        PageQuery pageQuery = param.pageQuery == null ? new PageQuery() : param.pageQuery;
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();

        SpaceEntity entity = findEntityByCode(param.spaceCode);
        if (entity == null) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<String> adminCodes = parseCodeList(entity.adminUserCodes);
        List<String> memberCodes = parseCodeList(entity.memberUserCodes);

        // 合并所有 userCodes，按 admin / member 打标，保持 owner 在最前。
        List<SpaceMemberVO> all = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String code : adminCodes) {
            if (seen.add(code)) {
                SpaceMemberVO vo = new SpaceMemberVO();
                vo.userCode = code;
                vo.roleType = SpaceRoleType.ADMIN;
                vo.owner = StrUtil.equals(code, entity.ownerUserCode);
                all.add(vo);
            }
        }
        for (String code : memberCodes) {
            if (seen.add(code)) {
                SpaceMemberVO vo = new SpaceMemberVO();
                vo.userCode = code;
                vo.roleType = SpaceRoleType.MEMBER;
                vo.owner = Boolean.FALSE;
                all.add(vo);
            }
        }

        // 批量补 userName / email / phone。
        List<UserDTO> users = userQueryService.listByCodes(new ArrayList<>(seen));
        Map<String, UserDTO> userByCode = new HashMap<>();
        for (UserDTO user : users) {
            userByCode.put(user.num, user);
        }
        for (SpaceMemberVO vo : all) {
            UserDTO user = userByCode.get(vo.userCode);
            if (user != null) {
                vo.userName = user.name;
                vo.email = user.email;
                vo.phone = user.phone;
            }
        }

        // 按 keyword 过滤（用户名 / 邮箱 / 手机号）。
        List<SpaceMemberVO> filtered;
        if (StrUtil.isBlank(param.keyword)) {
            filtered = all;
        } else {
            filtered = new ArrayList<>();
            String kw = param.keyword;
            for (SpaceMemberVO vo : all) {
                if (StrUtil.contains(vo.userName, kw)
                        || StrUtil.contains(vo.email, kw)
                        || StrUtil.contains(vo.phone, kw)
                        || StrUtil.contains(vo.userCode, kw)) {
                    filtered.add(vo);
                }
            }
        }

        // 内存分页。
        long total = filtered.size();
        int from = Math.min((pageNo - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        List<SpaceMemberVO> records = filtered.subList(from, to);
        return PageResult.of(total, pageNo, pageSize, new ArrayList<>(records));
    }

    /**
     * 校验目标用户是否为该空间成员（admin OR member）。
     *
     * @param spaceCode 空间业务编码
     * @param userCode  用户业务编码
     * @return 是否成员
     */
    public boolean isMember(String spaceCode, String userCode) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(userCode)) {
            return false;
        }
        SpaceEntity entity = findEntityByCode(spaceCode);
        if (entity == null) {
            return false;
        }
        List<String> admins = parseCodeList(entity.adminUserCodes);
        if (admins.contains(userCode)) {
            return true;
        }
        List<String> members = parseCodeList(entity.memberUserCodes);
        return members.contains(userCode);
    }

    /**
     * 校验目标用户是否为该空间管理员。
     *
     * @param spaceCode 空间业务编码
     * @param userCode  用户业务编码
     * @return 是否管理员
     */
    public boolean isAdmin(String spaceCode, String userCode) {
        if (StrUtil.isBlank(spaceCode) || StrUtil.isBlank(userCode)) {
            return false;
        }
        SpaceEntity entity = findEntityByCode(spaceCode);
        if (entity == null) {
            return false;
        }
        return parseCodeList(entity.adminUserCodes).contains(userCode);
    }

    /**
     * 按空间业务编码查询未删除空间实体。
     *
     * @param spaceCode 空间业务编码
     * @return 空间实体
     */
    private SpaceEntity findEntityByCode(String spaceCode) {
        LambdaQueryWrapper<SpaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpaceEntity::getNum, spaceCode)
                .eq(SpaceEntity::getIsDeleted, InfraConstant.NOT_DELETED)
                .last("limit 1");
        return spaceMapper.selectOne(wrapper);
    }

    /**
     * 解析 JSON 字符串数组为字符串列表。
     *
     * @param json JSON 字符串
     * @return 字符串列表
     */
    private List<String> parseCodeList(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        List<String> list = JSON.parseObject(json, new TypeReference<List<String>>() { });
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 把空间实体转换为 DTO。
     *
     * @param entity 空间实体
     * @return 空间 DTO
     */
    private SpaceDTO toSpaceDTO(SpaceEntity entity) {
        SpaceDTO dto = new SpaceDTO();
        dto.num = entity.num;
        dto.name = entity.name;
        dto.description = entity.description;
        dto.iconUrl = entity.iconUrl;
        dto.ownerUserCode = entity.ownerUserCode;
        dto.status = entity.status == null ? null : SpaceStatus.fromCode(entity.status);
        dto.adminUserCodes = parseCodeList(entity.adminUserCodes);
        dto.memberUserCodes = parseCodeList(entity.memberUserCodes);
        dto.createTime = entity.createTime;
        dto.updateTime = entity.updateTime;
        return dto;
    }

    /**
     * 把空间实体装配为卡片 VO。
     *
     * @param entity          空间实体
     * @param currentUserCode 当前用户业务编码
     * @return 空间卡片 VO
     */
    private SpaceCardVO toSpaceCardVO(SpaceEntity entity, String currentUserCode) {
        SpaceCardVO vo = new SpaceCardVO();
        vo.num = entity.num;
        vo.name = entity.name;
        vo.description = entity.description;
        vo.iconUrl = entity.iconUrl;
        vo.ownerUserCode = entity.ownerUserCode;
        List<String> admins = parseCodeList(entity.adminUserCodes);
        List<String> members = parseCodeList(entity.memberUserCodes);
        vo.adminCount = admins.size();
        vo.memberCount = members.size();
        if (admins.contains(currentUserCode)) {
            vo.currentUserRole = SpaceRoleType.ADMIN.name();
        } else if (members.contains(currentUserCode)) {
            vo.currentUserRole = SpaceRoleType.MEMBER.name();
        } else {
            vo.currentUserRole = null;
        }
        vo.createTime = entity.createTime;
        vo.updateTime = entity.updateTime;
        return vo;
    }

    /**
     * 抛出空间不存在异常。
     *
     * @param spaceCode 空间业务编码
     */
    @SuppressWarnings("unused")
    private static void throwSpaceNotFound(String spaceCode) {
        throw new BusinessException("SPACE_NOT_FOUND", "空间不存在: " + spaceCode);
    }
}
