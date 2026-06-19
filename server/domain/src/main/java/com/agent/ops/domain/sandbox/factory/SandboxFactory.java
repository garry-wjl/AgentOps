package com.agent.ops.domain.sandbox.factory;

import com.agent.ops.domain.sandbox.SandboxAggregate;

public interface SandboxFactory {
    SandboxAggregate create(String spaceCode, String name, String image, String baseUrlOverride, String remark);
    SandboxAggregate createByNum(String num);
}
