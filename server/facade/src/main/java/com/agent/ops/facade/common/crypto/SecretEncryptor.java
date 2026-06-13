package com.agent.ops.facade.common.crypto;

/**
 * 敏感字段对称加解密与脱敏工具契约。
 * 实现使用 AES-256-GCM；密文格式 `enc:v1:<base64(IV+ciphertext+tag)>`；
 * 脱敏格式 `<前 4>****<后 4>`，长度不足 8 位时全部 `****`。
 */
public interface SecretEncryptor {
    /**
     * 加密明文，返回 enc:v1:base64 格式密文。
     *
     * @param plaintext 明文
     * @return 密文
     */
    String encrypt(String plaintext);

    /**
     * 解密 enc:v1:* 格式密文；非该格式直接返回原值。
     *
     * @param ciphertext 密文
     * @return 明文
     */
    String decrypt(String ciphertext);

    /**
     * 脱敏展示。
     *
     * @param plaintext 明文或密文
     * @return 脱敏后的展示字符串
     */
    String mask(String plaintext);

    /**
     * 判断是否已加密格式。
     *
     * @param value 待判断的字符串
     * @return 是否已加密
     */
    boolean isEncrypted(String value);

    /**
     * 当系统设置中的加密密钥变更时由 SystemSettingsLoader 通过本方法刷新内存密钥。
     *
     * @param base64Key 32 字节密钥的 Base64 编码
     */
    void refreshKey(String base64Key);
}
