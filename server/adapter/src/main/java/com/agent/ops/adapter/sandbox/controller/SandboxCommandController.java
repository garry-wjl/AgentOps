package com.agent.ops.adapter.sandbox.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.sandbox.command.SandboxCommandService;
import com.agent.ops.client.sandbox.dto.SandboxDTO;
import com.agent.ops.client.sandbox.param.CreateSandboxParam;
import com.agent.ops.client.sandbox.param.SandboxActionParam;
import com.agent.ops.client.sandbox.param.UpdateSandboxParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sandboxes")
public class SandboxCommandController extends BaseController {
    @Resource
    private SandboxCommandService sandboxCommandService;

    @PostMapping("/create")
    public Result<SandboxDTO> create(@RequestBody CreateSandboxParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxCommandService.create(param));
    }

    @PostMapping("/update")
    public Result<SandboxDTO> update(@RequestBody UpdateSandboxParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxCommandService.update(param));
    }

    @PostMapping("/submit")
    public Result<SandboxDTO> submit(@RequestBody SandboxActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxCommandService.submit(param));
    }

    @PostMapping("/disable")
    public Result<SandboxDTO> disable(@RequestBody SandboxActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxCommandService.disable(param));
    }

    @PostMapping("/re-enable")
    public Result<SandboxDTO> reEnable(@RequestBody SandboxActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxCommandService.reEnable(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody SandboxActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        sandboxCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
