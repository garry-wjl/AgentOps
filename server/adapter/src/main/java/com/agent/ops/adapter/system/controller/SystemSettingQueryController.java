package com.agent.ops.adapter.system.controller;

import com.agent.ops.adapter.config.BaseController;
import com.agent.ops.application.system.query.SystemSettingQueryService;
import com.agent.ops.client.system.dto.PlatformBasicDTO;
import com.agent.ops.client.system.dto.SandboxDefaultDTO;
import com.agent.ops.client.system.dto.SmtpConfigDTO;
import com.agent.ops.client.system.dto.SpacePolicyDTO;
import com.agent.ops.facade.common.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统设置读控制器。
 */
@RestController
@RequestMapping("/api/system-settings")
public class SystemSettingQueryController extends BaseController {
    @Resource
    private SystemSettingQueryService systemSettingQueryService;

    @GetMapping("/platform-basic")
    public Result<PlatformBasicDTO> getPlatformBasic() {
        return Result.ok(systemSettingQueryService.getPlatformBasic());
    }

    @GetMapping("/smtp")
    public Result<SmtpConfigDTO> getSmtp() {
        return Result.ok(systemSettingQueryService.getSmtp());
    }

    @GetMapping("/space-policy")
    public Result<SpacePolicyDTO> getSpacePolicy() {
        return Result.ok(systemSettingQueryService.getSpacePolicy());
    }

    @GetMapping("/sandbox-default")
    public Result<SandboxDefaultDTO> getSandboxDefault() {
        return Result.ok(systemSettingQueryService.getSandboxDefault());
    }
}
