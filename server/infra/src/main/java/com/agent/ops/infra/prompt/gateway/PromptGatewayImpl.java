package com.agent.ops.infra.prompt.gateway;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.prompt.gateway.PromptGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt 网关实现。
 */
@Component
public class PromptGatewayImpl implements PromptGateway {
    /**
     * 占位符正则：`{{varName}}`，varName 须以字母或下划线开头，后接字母/数字/下划线。
     */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z_][A-Za-z0-9_]{0,31})\\s*}}");

    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Override
    public String generatePromptCode() {
        return bizCodeGenerator.generate(BizCodePrefix.PROMPT);
    }

    @Override
    public List<String> extractVariables(String content) {
        if (StrUtil.isBlank(content)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        Matcher matcher = VAR_PATTERN.matcher(content);
        while (matcher.find()) {
            set.add(matcher.group(1));
        }
        return new ArrayList<>(set);
    }
}
