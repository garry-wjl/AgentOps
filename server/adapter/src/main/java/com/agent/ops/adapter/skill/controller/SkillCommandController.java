package com.agent.ops.adapter.skill.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.skill.command.SkillCommandService;
import com.agent.ops.client.skill.dto.SkillDTO;
import com.agent.ops.client.skill.param.CreateSkillParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.client.skill.param.UpdateSkillBasicParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillCommandController extends BaseController {
    @Resource
    private SkillCommandService skillCommandService;

    @PostMapping("/create")
    public Result<SkillDTO> create(@RequestBody CreateSkillParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillCommandService.create(param));
    }

    @PostMapping("/update-basic")
    public Result<SkillDTO> updateBasic(@RequestBody UpdateSkillBasicParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillCommandService.updateBasic(param));
    }

    @PostMapping("/enable")
    public Result<SkillDTO> enable(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillCommandService.enable(param));
    }

    @PostMapping("/withdraw")
    public Result<SkillDTO> withdraw(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillCommandService.withdraw(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        skillCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
