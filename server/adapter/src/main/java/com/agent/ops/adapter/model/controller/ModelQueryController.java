package com.agent.ops.adapter.model.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.model.query.ModelQueryService;
import com.agent.ops.client.model.dto.ModelDTO;
import com.agent.ops.client.model.param.ModelQueryParam;
import com.agent.ops.client.model.vo.ModelVO;
import com.agent.ops.facade.common.Result;
import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.common.page.PageResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型读控制器。
 */
@RestController
@RequestMapping("/api/models")
public class ModelQueryController extends BaseController {
    @Resource
    private ModelQueryService modelQueryService;

    @GetMapping("/get")
    public Result<ModelDTO> get(@RequestParam("num") String num) {
        return Result.ok(modelQueryService.getByNum(num));
    }

    @GetMapping("/page")
    public Result<PageResult<ModelVO>> page(@RequestParam("spaceCode") String spaceCode,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        ModelQueryParam param = new ModelQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(modelQueryService.page(param));
    }

    @GetMapping("/list-enabled")
    public Result<List<ModelDTO>> listEnabled(@RequestParam("spaceCode") String spaceCode) {
        return Result.ok(modelQueryService.getEnabledList(spaceCode));
    }
}
