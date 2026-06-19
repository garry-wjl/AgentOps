package com.agent.ops.client.sandbox.dto;

import com.agent.ops.client.sandbox.enums.SandboxStatus;

import java.time.LocalDateTime;

public class SandboxDTO {
    public String num;
    public String spaceCode;
    public String name;
    public String image;
    public String baseUrlOverride;
    public String remark;
    public SandboxStatus status;
    public String lastStatusReason;
    public LocalDateTime lastHeartbeatTime;
    public LocalDateTime createTime;
    public LocalDateTime updateTime;
}
