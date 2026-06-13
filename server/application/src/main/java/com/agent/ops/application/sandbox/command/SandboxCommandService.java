package com.agent.ops.application.sandbox.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.system.query.SystemSettingQueryService;
import com.agent.ops.client.sandbox.dto.SandboxDTO;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.client.sandbox.param.CreateSandboxParam;
import com.agent.ops.client.sandbox.param.SandboxActionParam;
import com.agent.ops.client.sandbox.param.UpdateSandboxParam;
import com.agent.ops.client.system.dto.SandboxDefaultDTO;
import com.agent.ops.domain.sandbox.SandboxAggregate;
import com.agent.ops.domain.sandbox.factory.SandboxFactory;
import com.agent.ops.facade.common.probe.SandboxProbeClient;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 沙箱写应用服务。
 * <p>
 * runHeartbeat 在应用层组装：通过 SystemSettingQueryService 取默认 baseUrl + 通过 SandboxProbeClient 发起探活，
 * 再调用聚合根 markOnline / markOffline 切换状态（公共方案 §11.6 应用层禁止注入 Gateway）。
 */
@Service
public class SandboxCommandService {
    /**
     * 系统操作人占位。
     */
    private static final String SYSTEM_OPERATOR = "SYSTEM";

    @Resource
    private SandboxFactory sandboxFactory;

    @Resource
    private SystemSettingQueryService systemSettingQueryService;

    @Resource
    private SandboxProbeClient sandboxProbeClient;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public SandboxDTO create(CreateSandboxParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.spaceCode, "spaceCode 不能为空");
        return distributedLock.execute("sandbox:create:" + param.spaceCode + ":" + param.name, () -> {
            SandboxAggregate a = sandboxFactory.create(param.spaceCode, param.name, param.image, param.baseUrlOverride, param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SandboxDTO update(UpdateSandboxParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("sandbox:" + param.num, () -> {
            SandboxAggregate a = loadAggregate(param.num);
            if (StrUtil.isNotBlank(param.name)) a.setName(param.name);
            if (StrUtil.isNotBlank(param.image)) {
                // image 仅在 DRAFT/DISABLED 状态可改
                if (a.getStatus() != SandboxStatus.DRAFT && a.getStatus() != SandboxStatus.DISABLED) {
                    throw new BusinessException("SANDBOX_IMAGE_LOCKED", "仅草稿或禁用状态可修改 image");
                }
                a.setImage(param.image);
            }
            if (param.baseUrlOverride != null) a.setBaseUrlOverride(param.baseUrlOverride);
            if (param.remark != null) a.setRemark(param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SandboxDTO submit(SandboxActionParam param) {
        return distributedLock.execute("sandbox:" + param.num, () -> {
            SandboxAggregate a = loadAggregate(param.num);
            a.submit(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SandboxDTO disable(SandboxActionParam param) {
        return distributedLock.execute("sandbox:" + param.num, () -> {
            SandboxAggregate a = loadAggregate(param.num);
            a.disable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public SandboxDTO reEnable(SandboxActionParam param) {
        return distributedLock.execute("sandbox:" + param.num, () -> {
            SandboxAggregate a = loadAggregate(param.num);
            a.reEnable(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(SandboxActionParam param) {
        distributedLock.run("sandbox:" + param.num, () -> {
            SandboxAggregate a = loadAggregate(param.num);
            a.delete(param.getOperatorCode());
        });
    }

    /**
     * 周期探活：应用层组装 baseUrl + 调用 ProbeClient + 触发聚合状态切换。
     *
     * @param num 沙箱业务编码
     */
    @Transactional(rollbackFor = Exception.class)
    public void runHeartbeat(String num) {
        if (StrUtil.isBlank(num)) {
            return;
        }
        distributedLock.run("sandbox:heartbeat:" + num, () -> {
            SandboxAggregate a = sandboxFactory.createByNum(num);
            if (a == null) {
                return;
            }
            // 仅 INITIALIZING / ONLINE / OFFLINE 参与探活
            if (a.getStatus() != SandboxStatus.INITIALIZING
                    && a.getStatus() != SandboxStatus.ONLINE
                    && a.getStatus() != SandboxStatus.OFFLINE) {
                return;
            }
            String baseUrl = StrUtil.isNotBlank(a.getBaseUrlOverride()) ? a.getBaseUrlOverride() : null;
            if (StrUtil.isBlank(baseUrl)) {
                SandboxDefaultDTO defaults = systemSettingQueryService.getSandboxDefault();
                baseUrl = defaults == null ? null : defaults.baseUrl;
            }
            if (StrUtil.isBlank(baseUrl)) {
                a.markOffline("未配置接入地址", SYSTEM_OPERATOR);
                return;
            }
            boolean ok = sandboxProbeClient.probe(baseUrl);
            if (ok) {
                a.markOnline("探活成功", SYSTEM_OPERATOR);
            } else {
                a.markOffline("探活失败", SYSTEM_OPERATOR);
            }
        });
    }

    private SandboxAggregate loadAggregate(String num) {
        SandboxAggregate a = sandboxFactory.createByNum(num);
        if (a == null) {
            throw new BusinessException("SANDBOX_NOT_FOUND", "沙箱不存在");
        }
        return a;
    }

    private SandboxDTO toDTO(SandboxAggregate a) {
        SandboxDTO dto = new SandboxDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.image = a.getImage();
        dto.baseUrlOverride = a.getBaseUrlOverride();
        dto.remark = a.getRemark();
        dto.status = a.getStatus();
        dto.lastStatusReason = a.getLastStatusReason();
        dto.lastHeartbeatTime = a.getLastHeartbeatTime();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
