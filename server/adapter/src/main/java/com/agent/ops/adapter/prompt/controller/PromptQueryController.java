package com.agent.ops.adapter.prompt.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.prompt.query.PromptQueryService;
import com.agent.ops.client.prompt.dto.PromptDTO;
import com.agent.ops.client.prompt.param.PromptQueryParam;
import com.agent.ops.client.prompt.vo.PromptVO;
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
@RequestMapping("/api/prompts")
public class PromptQueryController extends BaseController {
    @Resource
    private PromptQueryService promptQueryService;

    @GetMapping("/get")
    public Result<PromptDTO> get(@RequestParam("num") String num) {
        return Result.ok(promptQueryService.getByNum(num));
    }

    @GetMapping("/get-by-key")
    public Result<PromptDTO> getByKey(@RequestParam("spaceCode") String spaceCode,
                                      @RequestParam("key") String key) {
        return Result.ok(promptQueryService.getEnabledByKey(spaceCode, key));
    }

    @GetMapping("/list-enabled")
    public Result<List<PromptDTO>> listEnabled(@RequestParam("spaceCode") String spaceCode) {
        return Result.ok(promptQueryService.getEnabledList(spaceCode));
    }

    @GetMapping("/page")
    public Result<PageResult<PromptVO>> page(@RequestParam("spaceCode") String spaceCode,
                                             @RequestParam(value = "keyword", required = false) String keyword,
                                             @RequestParam(value = "status", required = false) String status,
                                             @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                             @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        PromptQueryParam param = new PromptQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(promptQueryService.page(param));
    }
}
