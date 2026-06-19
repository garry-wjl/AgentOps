package com.agent.ops.adapter.model.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.model.command.ModelCommandService;
import com.agent.ops.client.model.dto.ModelDTO;
import com.agent.ops.client.model.param.CreateModelParam;
import com.agent.ops.client.model.param.ModelActionParam;
import com.agent.ops.client.model.param.UpdateModelParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型写控制器。
 */
@RestController
@RequestMapping("/api/models")
public class ModelCommandController extends BaseController {
    @Resource
    private ModelCommandService modelCommandService;

    @PostMapping("/create")
    public Result<ModelDTO> create(@RequestBody CreateModelParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(modelCommandService.create(param));
    }

    @PostMapping("/update")
    public Result<ModelDTO> update(@RequestBody UpdateModelParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(modelCommandService.update(param));
    }

    @PostMapping("/enable")
    public Result<ModelDTO> enable(@RequestBody ModelActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(modelCommandService.enable(param));
    }

    @PostMapping("/disable")
    public Result<ModelDTO> disable(@RequestBody ModelActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(modelCommandService.disable(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody ModelActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        modelCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
