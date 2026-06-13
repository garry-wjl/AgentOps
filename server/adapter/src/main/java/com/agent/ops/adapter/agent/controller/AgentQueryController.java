package com.agent.ops.adapter.agent.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.agent.query.AgentQueryService;
import com.agent.ops.client.agent.dto.AgentDTO;
import com.agent.ops.client.agent.dto.AgentVersionDTO;
import com.agent.ops.client.agent.param.AgentQueryParam;
import com.agent.ops.client.agent.vo.AgentVO;
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
@RequestMapping("/api/agents")
public class AgentQueryController extends BaseController {
    @Resource
    private AgentQueryService agentQueryService;

    @GetMapping("/get")
    public Result<AgentDTO> get(@RequestParam("num") String num) {
        return Result.ok(agentQueryService.getByNum(num));
    }

    @GetMapping("/page")
    public Result<PageResult<AgentVO>> page(@RequestParam("spaceCode") String spaceCode,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        AgentQueryParam param = new AgentQueryParam();
        param.setOperatorCode(getCurrentUserCode());
        param.spaceCode = spaceCode;
        param.keyword = keyword;
        param.status = status;
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNo);
        pageQuery.setPageSize(pageSize);
        param.pageQuery = pageQuery;
        return Result.ok(agentQueryService.page(param));
    }

    @GetMapping("/get-online-by-name")
    public Result<AgentVersionDTO> getOnlineByName(@RequestParam("spaceCode") String spaceCode,
                                                    @RequestParam("name") String name) {
        return Result.ok(agentQueryService.getOnlineByName(spaceCode, name));
    }

    @GetMapping("/versions")
    public Result<List<AgentVersionDTO>> listVersions(@RequestParam("agentCode") String agentCode) {
        return Result.ok(agentQueryService.listVersionsByAgentCode(agentCode));
    }

    @GetMapping("/version-get")
    public Result<AgentVersionDTO> getVersion(@RequestParam("num") String num) {
        return Result.ok(agentQueryService.getVersionByNum(num));
    }
}