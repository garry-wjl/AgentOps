package com.agent.ops.adapter.space.controller;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.space.SpaceQueryService;
import com.agent.ops.client.space.dto.SpaceDTO;
import com.agent.ops.client.space.param.SpaceMembersQueryParam;
import com.agent.ops.client.space.param.SpaceQueryParam;
import com.agent.ops.client.space.vo.SpaceCardVO;
import com.agent.ops.client.space.vo.SpaceMemberVO;
import com.agent.ops.facade.common.Result;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import com.agent.ops.facade.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 空间读操作控制器：详情 + 我的空间分页 + 成员分页。
 */
@RestController
@RequestMapping("/api/spaces")
public class SpaceQueryController extends BaseController {
    /**
     * 空间查询应用服务。
     */
    @Resource
    private SpaceQueryService spaceQueryService;

    /**
     * 根据业务编码查询空间详情。
     *
     * @param code 空间业务编码
     * @return 空间 DTO
     */
    @GetMapping("/get")
    public Result<SpaceDTO> get(@RequestParam("code") String code) {
        return Result.ok(spaceQueryService.getByCode(code));
    }

    /**
     * 分页查询当前用户的"我的空间"卡片列表。
     *
     * @param keyword  关键字，可空
     * @param pageNo   页码，从 1 开始
     * @param pageSize 每页大小
     * @return 卡片分页结果
     */
    @GetMapping("/page-mine")
    public Result<PageResult<SpaceCardVO>> pageMine(@RequestParam(value = "keyword", required = false) String keyword,
                                                    @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                    @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        String currentUserCode = getCurrentUserCode();
        if (StrUtil.isBlank(currentUserCode)) {
            throw new BusinessException("UNAUTHENTICATED", "请先登录");
        }
        SpaceQueryParam param = new SpaceQueryParam();
        param.setOperatorCode(currentUserCode);
        param.keyword = keyword;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize == null ? 12 : pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(spaceQueryService.pageMine(param));
    }

    /**
     * 分页查询空间成员列表。
     *
     * @param spaceCode 空间业务编码
     * @param keyword   关键字，可空
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 成员分页结果
     */
    @GetMapping("/page-members")
    public Result<PageResult<SpaceMemberVO>> pageMembers(@RequestParam("spaceCode") String spaceCode,
                                                         @RequestParam(value = "keyword", required = false) String keyword,
                                                         @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                         @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        SpaceMembersQueryParam param = new SpaceMembersQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(spaceQueryService.pageMembers(param));
    }
}
