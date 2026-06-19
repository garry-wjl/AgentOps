package com.agent.ops.domain.sandbox.repository;

import com.agent.ops.domain.sandbox.SandboxAggregate;

import java.util.List;

public interface SandboxRepository {
    void save(SandboxAggregate aggregate);
    SandboxAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
    /**
     * 查询所有可探活状态（INITIALIZING / ONLINE / OFFLINE）的沙箱业务编码。
     *
     * @return 业务编码列表
     */
    List<String> listProbeableNums();
}
