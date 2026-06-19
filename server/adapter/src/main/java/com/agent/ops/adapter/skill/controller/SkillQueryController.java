package com.agent.ops.adapter.skill.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.skill.query.SkillQueryService;
import com.agent.ops.client.skill.dto.SkillDTO;
import com.agent.ops.client.skill.dto.SkillResourceFileDTO;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.skill.param.SkillQueryParam;
import com.agent.ops.client.skill.vo.SkillVO;
import com.agent.ops.facade.common.Result;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillQueryController extends BaseController {
    @Resource
    private SkillQueryService skillQueryService;

    @GetMapping("/get")
    public Result<SkillDTO> get(@RequestParam("num") String num) {
        return Result.ok(skillQueryService.getByNum(num));
    }

    @GetMapping("/page")
    public Result<PageResult<SkillVO>> page(@RequestParam("spaceCode") String spaceCode,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        SkillQueryParam param = new SkillQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(skillQueryService.page(param));
    }

    @GetMapping("/list-effective")
    public Result<List<SkillDTO>> listEffective(@RequestParam("spaceCode") String spaceCode) {
        return Result.ok(skillQueryService.getEffectiveListForReference(spaceCode));
    }

    @GetMapping("/versions")
    public Result<List<SkillVersionDTO>> listVersions(@RequestParam("skillCode") String skillCode) {
        return Result.ok(skillQueryService.listVersionsBySkillCode(skillCode));
    }

    @GetMapping("/version-get")
    public Result<SkillVersionDTO> getVersion(@RequestParam("num") String num) {
        return Result.ok(skillQueryService.getVersionByNum(num));
    }

    @GetMapping("/version-effective")
    public Result<SkillVersionDTO> getEffectiveVersion(@RequestParam("skillCode") String skillCode) {
        return Result.ok(skillQueryService.getEffectiveVersionBySkillCode(skillCode));
    }

    @GetMapping("/resources")
    public Result<List<SkillResourceFileDTO>> listResources(@RequestParam("versionCode") String versionCode) {
        return Result.ok(skillQueryService.listResourceFiles(versionCode));
    }
}
