package com.agent.ops.client.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 装配快照值对象 DTO（在版本聚合内 1:1 持有）。
 */
public class AssemblySnapshotDTO {
    public String modelCode;
    /**
     * 模型参数 JSON：{ temperature, topP, maxTokens, ... }。
     */
    public String modelParamsJson;
    public String systemPromptContent;
    public String systemPromptSourceCode;
    public String userPromptContent;
    public String userPromptSourceCode;
    public List<String> skillCodes = new ArrayList<>();
    public List<String> toolCodes = new ArrayList<>();
    public String sandboxCode;
    public Integer shortMemoryTurns;
}
