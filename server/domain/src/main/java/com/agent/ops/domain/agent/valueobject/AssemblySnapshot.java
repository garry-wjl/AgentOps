package com.agent.ops.domain.agent.valueobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 装配快照值对象（领域内）。
 */
public class AssemblySnapshot {
    private String modelCode;
    private String modelParamsJson;
    private String systemPromptContent;
    private String systemPromptSourceCode;
    private String userPromptContent;
    private String userPromptSourceCode;
    private List<String> skillCodes = new ArrayList<>();
    private List<String> toolCodes = new ArrayList<>();
    private String sandboxCode;
    private Integer shortMemoryTurns;

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getModelParamsJson() { return modelParamsJson; }
    public void setModelParamsJson(String modelParamsJson) { this.modelParamsJson = modelParamsJson; }
    public String getSystemPromptContent() { return systemPromptContent; }
    public void setSystemPromptContent(String systemPromptContent) { this.systemPromptContent = systemPromptContent; }
    public String getSystemPromptSourceCode() { return systemPromptSourceCode; }
    public void setSystemPromptSourceCode(String systemPromptSourceCode) { this.systemPromptSourceCode = systemPromptSourceCode; }
    public String getUserPromptContent() { return userPromptContent; }
    public void setUserPromptContent(String userPromptContent) { this.userPromptContent = userPromptContent; }
    public String getUserPromptSourceCode() { return userPromptSourceCode; }
    public void setUserPromptSourceCode(String userPromptSourceCode) { this.userPromptSourceCode = userPromptSourceCode; }
    public List<String> getSkillCodes() { return skillCodes == null ? new ArrayList<>() : skillCodes; }
    public void setSkillCodes(List<String> skillCodes) { this.skillCodes = skillCodes == null ? new ArrayList<>() : new ArrayList<>(skillCodes); }
    public List<String> getToolCodes() { return toolCodes == null ? new ArrayList<>() : toolCodes; }
    public void setToolCodes(List<String> toolCodes) { this.toolCodes = toolCodes == null ? new ArrayList<>() : new ArrayList<>(toolCodes); }
    public String getSandboxCode() { return sandboxCode; }
    public void setSandboxCode(String sandboxCode) { this.sandboxCode = sandboxCode; }
    public Integer getShortMemoryTurns() { return shortMemoryTurns; }
    public void setShortMemoryTurns(Integer shortMemoryTurns) { this.shortMemoryTurns = shortMemoryTurns; }
}
