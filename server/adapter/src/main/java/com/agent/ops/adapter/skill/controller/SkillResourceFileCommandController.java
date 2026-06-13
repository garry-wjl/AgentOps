package com.agent.ops.adapter.skill.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.skill.command.SkillResourceFileCommandService;
import com.agent.ops.client.skill.dto.SkillResourceFileDTO;
import com.agent.ops.client.skill.param.CreateResourceFileParam;
import com.agent.ops.client.skill.param.RenameResourceFileParam;
import com.agent.ops.client.skill.param.SkillActionParam;
import com.agent.ops.client.skill.param.UpdateResourceFileContentParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-resources")
public class SkillResourceFileCommandController extends BaseController {
    @Resource
    private SkillResourceFileCommandService skillResourceFileCommandService;

    @PostMapping("/create")
    public Result<SkillResourceFileDTO> create(@RequestBody CreateResourceFileParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillResourceFileCommandService.create(param));
    }

    @PostMapping("/update-content")
    public Result<SkillResourceFileDTO> updateContent(@RequestBody UpdateResourceFileContentParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillResourceFileCommandService.updateContent(param));
    }

    @PostMapping("/rename")
    public Result<SkillResourceFileDTO> rename(@RequestBody RenameResourceFileParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(skillResourceFileCommandService.rename(param));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody SkillActionParam param) {
        param.setOperatorCode(getCurrentUserCode());
        skillResourceFileCommandService.delete(param);
        return Result.ok(Boolean.TRUE);
    }
}
