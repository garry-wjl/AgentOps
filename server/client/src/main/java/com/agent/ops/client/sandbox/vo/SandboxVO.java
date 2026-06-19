package com.agent.ops.client.sandbox.vo;

import com.agent.ops.client.sandbox.enums.SandboxStatus;

import java.time.LocalDateTime;

public class SandboxVO {
    public String num;
    public String name;
    public String image;
    public String baseUrlOverride;
    public String remark;
    public SandboxStatus status;
    public String lastStatusReason;
    public LocalDateTime lastHeartbeatTime;
    public LocalDateTime updateTime;
}
