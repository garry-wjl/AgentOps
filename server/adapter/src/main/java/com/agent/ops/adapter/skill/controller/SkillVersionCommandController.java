package com.agent.ops.adapter.skill.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.skill.command.SkillVersionCommandService;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.skill.param.DeriveVersionParam;
import com.agent.ops.client.skill.param.EditVersionParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-versions")
public class SkillVersionCommandController extends BaseController {
    @Resource
    private SkillVersionCommandService skillVersionCommandService;

    @PostMapping("/derive-draft")
    public Result<SkillVersionDTO> deriveDraft(@RequestBody DeriveVersionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillVersionCommandService.deriveDraft(param));
    }

    @PostMapping("/edit-content")
    public Result<SkillVersionDTO> editContent(@RequestBody EditVersionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillVersionCommandService.editContent(param));
    }

    @PostMapping("/publish")
    public Result<SkillVersionDTO> publish(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillVersionCommandService.publish(param));
    }

    @PostMapping("/withdraw")
    public Result<SkillVersionDTO> withdraw(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillVersionCommandService.withdraw(param));
    }

    @PostMapping("/delete-draft")
    public Result<Boolean> delete(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        skillVersionCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
