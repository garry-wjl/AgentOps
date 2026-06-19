package com.agent.ops.adapter.system.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.system.command.PlatformBasicCommandService;
import com.agent.ops.application.system.command.SandboxDefaultCommandService;
import com.agent.ops.application.system.command.SmtpCommandService;
import com.agent.ops.application.system.command.SpacePolicyCommandService;
import com.agent.ops.client.system.dto.PlatformBasicDTO;
import com.agent.ops.client.system.dto.SandboxDefaultDTO;
import com.agent.ops.client.system.dto.SmtpConfigDTO;
import com.agent.ops.client.system.dto.SpacePolicyDTO;
import com.agent.ops.client.system.param.SendTestMailParam;
import com.agent.ops.client.system.param.UpdatePlatformBasicParam;
import com.agent.ops.client.system.param.UpdateSandboxDefaultParam;
import com.agent.ops.client.system.param.UpdateSmtpParam;
import com.agent.ops.client.system.param.UpdateSpacePolicyParam;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统设置写控制器。
 */
@RestController
@RequestMapping("/api/system-settings")
public class SystemSettingCommandController extends BaseController {
    @Resource
    private PlatformBasicCommandService platformBasicCommandService;

    @Resource
    private SmtpCommandService smtpCommandService;

    @Resource
    private SpacePolicyCommandService spacePolicyCommandService;

    @Resource
    private SandboxDefaultCommandService sandboxDefaultCommandService;

    @PostMapping("/platform-basic/update")
    public Result<PlatformBasicDTO> updatePlatformBasic(@RequestBody UpdatePlatformBasicParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(platformBasicCommandService.update(param));
    }

    @PostMapping("/smtp/update")
    public Result<SmtpConfigDTO> updateSmtp(@RequestBody UpdateSmtpParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(smtpCommandService.update(param));
    }

    @PostMapping("/smtp/send-test-mail")
    public Result<Boolean> sendTestMail(@RequestBody SendTestMailParam param) {
        param.setOperatorCode(getCurrentUserCode());
        smtpCommandService.sendTestMail(param);
        return Result.ok(Boolean.TRUE);
    }

    @PostMapping("/space-policy/update")
    public Result<SpacePolicyDTO> updateSpacePolicy(@RequestBody UpdateSpacePolicyParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(spacePolicyCommandService.update(param));
    }

    @PostMapping("/sandbox-default/update")
    public Result<SandboxDefaultDTO> updateSandboxDefault(@RequestBody UpdateSandboxDefaultParam param) {
        param.setOperatorCode(getCurrentUserCode());
        return Result.ok(sandboxDefaultCommandService.update(param));
    }
}
