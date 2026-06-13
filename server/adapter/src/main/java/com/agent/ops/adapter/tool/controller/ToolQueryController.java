package com.agent.ops.adapter.tool.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.tool.query.ToolQueryService;
import com.agent.ops.client.tool.dto.ToolDTO;
import com.agent.ops.client.tool.param.ToolQueryParam;
import com.agent.ops.client.tool.vo.ToolVO;
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
@RequestMapping("/api/tools")
public class ToolQueryController extends BaseController {
    @Resource
    private ToolQueryService toolQueryService;

    @GetMapping("/get")
    public Result<ToolDTO> get(@RequestParam("num") String num) {
        return Result.ok(toolQueryService.getByNum(num));
    }

    @GetMapping("/page")
    public Result<PageResult<ToolVO>> page(@RequestParam("spaceCode") String spaceCode,
                                           @RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "type", required = false) String type,
                                           @RequestParam(value = "subType", required = false) String subType,
                                           @RequestParam(value = "status", required = false) String status,
                                           @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                           @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        ToolQueryParam param = new ToolQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.type = type;
        param.subType = subType;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(toolQueryService.page(param));
    }

    @GetMapping("/list-effective")
    public Result<List<ToolDTO>> listEffective(@RequestParam("spaceCode") String spaceCode) {
        return Result.ok(toolQueryService.getEffectiveList(spaceCode));
    }
}
