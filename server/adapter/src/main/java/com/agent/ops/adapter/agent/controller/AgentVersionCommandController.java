package com.agent.ops.adapter.agent.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.agent.command.AgentVersionCommandService;
import com.agent.ops.client.agent.dto.AgentVersionDTO;
import com.agent.ops.client.agent.param.AgentActionParam;
import com.agent.ops.client.agent.param.DeriveAgentVersionParam;
import com.agent.ops.client.agent.param.EditAssemblyParam;
import com.agent.ops.client.agent.vo.PrePublishCheckVO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent-versions")
public class AgentVersionCommandController extends BaseController {
    @Resource
    private AgentVersionCommandService agentVersionCommandService;

    @PostMapping("/derive-draft")
    public Result<AgentVersionDTO> deriveDraft(@RequestBody DeriveAgentVersionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentVersionCommandService.deriveDraft(param));
    }

    @PostMapping("/edit-assembly")
    public Result<AgentVersionDTO> editAssembly(@RequestBody EditAssemblyParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentVersionCommandService.editAssembly(param));
    }

    @GetMapping("/pre-publish-check")
    public Result<PrePublishCheckVO> prePublishCheck(@RequestParam("num") String num) {
        return Result.ok(agentVersionCommandService.prePublishCheck(num));
    }

    @PostMapping("/publish")
    public Result<AgentVersionDTO> publish(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentVersionCommandService.publish(param));
    }

    @PostMapping("/offline")
    public Result<AgentVersionDTO> offline(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentVersionCommandService.offline(param));
    }

    @PostMapping("/delete-draft")
    public Result<Boolean> delete(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        agentVersionCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}