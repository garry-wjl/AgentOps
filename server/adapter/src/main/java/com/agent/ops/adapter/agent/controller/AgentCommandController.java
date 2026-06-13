package com.agent.ops.adapter.agent.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.agent.command.AgentCommandService;
import com.agent.ops.client.agent.dto.AgentDTO;
import com.agent.ops.client.agent.param.AgentActionParam;
import com.agent.ops.client.agent.param.CreateAgentParam;
import com.agent.ops.client.agent.param.UpdateAgentBasicParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agents")
public class AgentCommandController extends BaseController {
    @Resource
    private AgentCommandService agentCommandService;

    @PostMapping("/create")
    public Result<AgentDTO> create(@RequestBody CreateAgentParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentCommandService.create(param));
    }

    @PostMapping("/update-basic")
    public Result<AgentDTO> updateBasic(@RequestBody UpdateAgentBasicParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentCommandService.updateBasic(param));
    }

    @PostMapping("/enable")
    public Result<AgentDTO> enable(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentCommandService.enable(param));
    }

    @PostMapping("/withdraw")
    public Result<AgentDTO> withdraw(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(agentCommandService.withdraw(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody AgentActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        agentCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}