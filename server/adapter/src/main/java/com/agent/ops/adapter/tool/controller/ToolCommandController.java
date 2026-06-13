package com.agent.ops.adapter.tool.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.tool.command.ToolCommandService;
import com.agent.ops.client.tool.dto.TestResultDTO;
import com.agent.ops.client.tool.dto.ToolDTO;
import com.agent.ops.client.tool.param.CreateToolParam;
import com.agent.ops.client.tool.param.ToolActionParam;
import com.agent.ops.client.tool.param.ToolTestParam;
import com.agent.ops.client.tool.param.UpdateToolParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class ToolCommandController extends BaseController {
    @Resource
    private ToolCommandService toolCommandService;

    @PostMapping("/create")
    public Result<ToolDTO> create(@RequestBody CreateToolParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.create(param));
    }

    @PostMapping("/update")
    public Result<ToolDTO> update(@RequestBody UpdateToolParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.update(param));
    }

    @PostMapping("/publish")
    public Result<ToolDTO> publish(@RequestBody ToolActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.publish(param));
    }

    @PostMapping("/withdraw")
    public Result<ToolDTO> withdraw(@RequestBody ToolActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.withdraw(param));
    }

    @PostMapping("/republish")
    public Result<ToolDTO> republish(@RequestBody ToolActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.republish(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody ToolActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        toolCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }

    @PostMapping("/test")
    public Result<TestResultDTO> test(@RequestBody ToolTestParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(toolCommandService.test(param));
    }
}
