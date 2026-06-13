package com.agent.ops.adapter.sandbox.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.sandbox.query.SandboxQueryService;
import com.agent.ops.client.sandbox.dto.SandboxDTO;
import com.agent.ops.client.sandbox.param.SandboxQueryParam;
import com.agent.ops.client.sandbox.vo.SandboxVO;
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
@RequestMapping("/api/sandboxes")
public class SandboxQueryController extends BaseController {
    @Resource
    private SandboxQueryService sandboxQueryService;

    @GetMapping("/get")
    public Result<SandboxDTO> get(@RequestParam("num") String num) {
        return Result.ok(sandboxQueryService.getByNum(num));
    }

    @GetMapping("/page")
    public Result<PageResult<SandboxVO>> page(@RequestParam("spaceCode") String spaceCode,
                                              @RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "status", required = false) String status,
                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                              @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        SandboxQueryParam param = new SandboxQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(sandboxQueryService.page(param));
    }

    @GetMapping("/list-available")
    public Result<List<SandboxDTO>> listAvailable(@RequestParam("spaceCode") String spaceCode) {
        return Result.ok(sandboxQueryService.getAvailableList(spaceCode));
    }
}
