package com.agent.ops.adapter.prompt.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.prompt.command.PromptCommandService;
import com.agent.ops.client.prompt.dto.PromptDTO;
import com.agent.ops.client.prompt.param.CreatePromptParam;
import com.agent.ops.client.prompt.param.PromptActionParam;
import com.agent.ops.client.prompt.param.UpdatePromptParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompts")
public class PromptCommandController extends BaseController {
    @Resource
    private PromptCommandService promptCommandService;

    @PostMapping("/create")
    public Result<PromptDTO> create(@RequestBody CreatePromptParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(promptCommandService.create(param));
    }

    @PostMapping("/update")
    public Result<PromptDTO> update(@RequestBody UpdatePromptParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(promptCommandService.update(param));
    }

    @PostMapping("/submit")
    public Result<PromptDTO> submit(@RequestBody PromptActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(promptCommandService.submit(param));
    }

    @PostMapping("/enable")
    public Result<PromptDTO> enable(@RequestBody PromptActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(promptCommandService.enable(param));
    }

    @PostMapping("/disable")
    public Result<PromptDTO> disable(@RequestBody PromptActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(promptCommandService.disable(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody PromptActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        promptCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
