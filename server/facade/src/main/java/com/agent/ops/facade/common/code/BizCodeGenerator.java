package com.agent.ops.facade.common.code;

/**
 * 业务编码生成器契约：编码格式为 `<前缀> + yyyyMMddHHmmssSSS + 4 位随机数`。
 */
public interface BizCodeGenerator {
    /**
     * 按指定前缀生成业务编码。
     *
     * @param prefix 业务编码前缀
     * @return 生成的业务编码
     */
    String generate(BizCodePrefix prefix);
}
