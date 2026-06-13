package com.agent.ops.application.agent.assembler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.application.model.query.ModelQueryService;
import com.agent.ops.application.prompt.query.PromptQueryService;
import com.agent.ops.application.sandbox.query.SandboxQueryService;
import com.agent.ops.application.skill.query.SkillQueryService;
import com.agent.ops.application.tool.query.ToolQueryService;
import com.agent.ops.client.agent.vo.PrePublishCheckVO;
import com.agent.ops.client.model.dto.ModelDTO;
import com.agent.ops.client.model.enums.ModelStatus;
import com.agent.ops.client.sandbox.dto.SandboxDTO;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.client.skill.dto.SkillVersionDTO;
import com.agent.ops.client.tool.dto.ToolDTO;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Agent 装配预检器（应用层组件）。
 * <p>
 * 通过 @Resource 注入 5 类对方领域 QueryService 完成跨领域校验，
 * 不通过 AgentGateway（公共方案 §11.6）。
 */
@Component
public class AssemblyValidator {
    @Resource
    private ModelQueryService modelQueryService;

    @Resource
    private PromptQueryService promptQueryService;

    @Resource
    private SkillQueryService skillQueryService;

    @Resource
    private ToolQueryService toolQueryService;

    @Resource
    private SandboxQueryService sandboxQueryService;

    /**
     * 装配预检。errors 非空则不允许发布；warnings 仅提示。
     *
     * @param snapshot 装配快照
     * @param spaceCode 所属空间业务编码（用于跨空间校验）
     * @return 预检结果
     */
    public PrePublishCheckVO validate(AssemblySnapshot snapshot, String spaceCode) {
        PrePublishCheckVO vo = new PrePublishCheckVO();
        if (snapshot == null) {
            vo.passed = false;
            vo.errors.add(error("snapshot", "ASSEMBLY_NULL", "装配快照不能为空"));
            return vo;
        }
        // 系统提示词非空
        if (StrUtil.isBlank(snapshot.getSystemPromptContent())) {
            vo.errors.add(error("systemPromptContent", "SYSTEM_PROMPT_REQUIRED", "系统提示词不能为空"));
        }
        // 短期记忆轮数
        if (snapshot.getShortMemoryTurns() == null
                || snapshot.getShortMemoryTurns() < 0
                || snapshot.getShortMemoryTurns() > 50) {
            vo.errors.add(error("shortMemoryTurns", "SHORT_MEMORY_TURNS_INVALID", "短期记忆轮数必须在 0~50 之间"));
        }
        // 模型校验
        if (StrUtil.isBlank(snapshot.getModelCode())) {
            vo.errors.add(error("modelCode", "MODEL_REQUIRED", "未选择模型"));
        } else {
            ModelDTO model = modelQueryService.getByNum(snapshot.getModelCode());
            if (model == null) {
                vo.errors.add(error("modelCode", "MODEL_NOT_FOUND", "模型不存在: " + snapshot.getModelCode()));
            } else if (model.status != ModelStatus.ENABLED) {
                vo.errors.add(error("modelCode", "MODEL_NOT_ENABLED", "模型未启用: " + snapshot.getModelCode()));
            } else if (StrUtil.isNotBlank(spaceCode) && !StrUtil.equals(model.spaceCode, spaceCode)) {
                vo.errors.add(error("modelCode", "MODEL_CROSS_SPACE", "模型不属于本空间"));
            }
        }
        // Skill 校验：本身存在 + 至少一个生效版本
        if (CollUtil.isNotEmpty(snapshot.getSkillCodes())) {
            for (String skillCode : snapshot.getSkillCodes()) {
                SkillVersionDTO eff = skillQueryService.getEffectiveVersionBySkillCode(skillCode);
                if (eff == null) {
                    vo.errors.add(error("skillCodes", "SKILL_NO_EFFECTIVE_VERSION",
                            "Skill " + skillCode + " 没有生效版本"));
                }
            }
        }
        // 工具校验：生效态
        if (CollUtil.isNotEmpty(snapshot.getToolCodes())) {
            for (String toolCode : snapshot.getToolCodes()) {
                ToolDTO tool = toolQueryService.getByNum(toolCode);
                if (tool == null) {
                    vo.errors.add(error("toolCodes", "TOOL_NOT_FOUND", "工具不存在: " + toolCode));
                } else if (tool.status != ToolStatus.EFFECTIVE) {
                    vo.errors.add(error("toolCodes", "TOOL_NOT_EFFECTIVE", "工具未生效: " + toolCode));
                }
            }
        }
        // 沙箱校验：非禁用且非草稿
        if (StrUtil.isNotBlank(snapshot.getSandboxCode())) {
            SandboxDTO sandbox = sandboxQueryService.getByNum(snapshot.getSandboxCode());
            if (sandbox == null) {
                vo.errors.add(error("sandboxCode", "SANDBOX_NOT_FOUND", "沙箱不存在: " + snapshot.getSandboxCode()));
            } else if (sandbox.status == SandboxStatus.DISABLED) {
                vo.errors.add(error("sandboxCode", "SANDBOX_DISABLED", "沙箱已禁用: " + snapshot.getSandboxCode()));
            } else if (sandbox.status == SandboxStatus.DRAFT) {
                vo.errors.add(error("sandboxCode", "SANDBOX_DRAFT", "沙箱为草稿态: " + snapshot.getSandboxCode()));
            } else if (sandbox.status == SandboxStatus.OFFLINE) {
                vo.warnings.add(warning("sandboxCode", "SANDBOX_OFFLINE", "沙箱当前离线，运行时调用可能失败"));
            }
        }
        // 系统/用户提示词来源 Prompt（仅留痕，不强校验启用态）
        if (StrUtil.isNotBlank(snapshot.getSystemPromptSourceCode())) {
            if (promptQueryService.getByNum(snapshot.getSystemPromptSourceCode()) == null) {
                vo.warnings.add(warning("systemPromptSourceCode", "SYSTEM_PROMPT_SOURCE_NOT_FOUND",
                        "系统提示词来源 Prompt 已删除，仅留痕"));
            }
        }
        if (StrUtil.isNotBlank(snapshot.getUserPromptSourceCode())) {
            if (promptQueryService.getByNum(snapshot.getUserPromptSourceCode()) == null) {
                vo.warnings.add(warning("userPromptSourceCode", "USER_PROMPT_SOURCE_NOT_FOUND",
                        "用户提示词来源 Prompt 已删除，仅留痕"));
            }
        }
        vo.passed = vo.errors.isEmpty();
        return vo;
    }

    private PrePublishCheckVO.Item error(String field, String code, String message) {
        PrePublishCheckVO.Item item = new PrePublishCheckVO.Item();
        item.field = field;
        item.code = code;
        item.message = message;
        return item;
    }

    private PrePublishCheckVO.Item warning(String field, String code, String message) {
        return error(field, code, message);
    }
}
