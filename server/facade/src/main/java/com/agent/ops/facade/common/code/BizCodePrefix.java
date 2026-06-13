package com.agent.ops.facade.common.code;

/**
 * 平台业务编码前缀枚举，集中管理各领域聚合根的业务编码前缀。
 */
public enum BizCodePrefix {
    /**
     * 空间业务编码前缀。
     */
    SPACE("SP"),
    /**
     * 用户业务编码前缀。
     */
    USER("US"),
    /**
     * 模型业务编码前缀。
     */
    MODEL("MD"),
    /**
     * 提示词业务编码前缀。
     */
    PROMPT("PR"),
    /**
     * Skill 主体业务编码前缀。
     */
    SKILL("SK"),
    /**
     * Skill 版本业务编码前缀。
     */
    SKILL_VERSION("SKV"),
    /**
     * 工具业务编码前缀。
     */
    TOOL("TL"),
    /**
     * 沙箱业务编码前缀。
     */
    SANDBOX("SB"),
    /**
     * Agent 主体业务编码前缀。
     */
    AGENT("AG"),
    /**
     * Agent 版本业务编码前缀。
     */
    AGENT_VERSION("AGV"),
    /**
     * 系统设置业务编码前缀。
     */
    SYSTEM_SETTING("SS"),
    /**
     * 审计日志业务编码前缀。
     */
    AUDIT_LOG("AL");

    /**
     * 编码前缀字符串。
     */
    private final String value;

    BizCodePrefix(String value) {
        this.value = value;
    }

    /**
     * 返回编码前缀字符串。
     *
     * @return 编码前缀
     */
    public String getValue() {
        return value;
    }
}
