package com.agent.ops.infra.common.code;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 业务编码生成器实现：编码格式为 `<前缀> + yyyyMMddHHmmssSSS + 4 位随机数`。
 */
@Component
public class BizCodeGeneratorImpl implements BizCodeGenerator {
    /**
     * 业务编码时间片格式。
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 按指定前缀生成业务编码。
     *
     * @param prefix 业务编码前缀
     * @return 生成的业务编码
     */
    @Override
    public String generate(BizCodePrefix prefix) {
        Assert.notNull(prefix, "业务编码前缀不能为空");
        String timePart = LocalDateTimeUtil.now().format(TIME_FORMATTER);
        String randomPart = RandomUtil.randomNumbers(4);
        return prefix.getValue() + timePart + randomPart;
    }
}
