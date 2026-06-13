package com.agent.ops.infra.common.crypto;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AES-256-GCM 本地对称加密实现。密钥首启从 `agentops.crypto.encryption-key` 读取，
 * 系统设置模块（SystemSettingsLoader）启动时与变更时通过 {@link #refreshKey(String)} 刷新。
 */
@Component
public class SecretEncryptorImpl implements SecretEncryptor {
    /**
     * 密文版本前缀。
     */
    private static final String PREFIX_V1 = "enc:v1:";

    /**
     * IV 字节数（GCM 推荐 12 字节）。
     */
    private static final int IV_LENGTH = 12;

    /**
     * GCM 认证标签位数。
     */
    private static final int GCM_TAG_BITS = 128;

    /**
     * AES-256 密钥字节数。
     */
    private static final int KEY_LENGTH = 32;

    /**
     * 启动时从配置读取的密钥（可空），运行时被 system_settings 模块覆盖。
     */
    @Value("${agentops.crypto.encryption-key:}")
    private String bootstrapKey;

    /**
     * 当前生效密钥（AtomicReference 保证可见性）。
     */
    private final AtomicReference<SecretKeySpec> currentKey = new AtomicReference<>();

    /**
     * 加密明文。
     *
     * @param plaintext 明文
     * @return 密文
     */
    @Override
    public String encrypt(String plaintext) {
        if (StrUtil.isEmpty(plaintext)) {
            return plaintext;
        }
        if (isEncrypted(plaintext)) {
            return plaintext;
        }
        SecretKeySpec key = ensureKey();
        try {
            byte[] iv = RandomUtil.randomBytes(IV_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return PREFIX_V1 + Base64.encode(combined);
        } catch (Exception e) {
            throw new BusinessException("ENCRYPT_FAILED", "加密失败: " + e.getMessage());
        }
    }

    /**
     * 解密密文。
     *
     * @param ciphertext 密文
     * @return 明文
     */
    @Override
    public String decrypt(String ciphertext) {
        if (StrUtil.isEmpty(ciphertext) || !isEncrypted(ciphertext)) {
            return ciphertext;
        }
        SecretKeySpec key = ensureKey();
        try {
            String base64Body = ciphertext.substring(PREFIX_V1.length());
            byte[] combined = Base64.decode(base64Body);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("DECRYPT_FAILED", "解密失败: " + e.getMessage());
        }
    }

    /**
     * 脱敏展示。
     *
     * @param plaintext 明文或密文（直接以字符串处理）
     * @return 脱敏字符串
     */
    @Override
    public String mask(String plaintext) {
        if (StrUtil.isEmpty(plaintext)) {
            return plaintext;
        }
        if (isEncrypted(plaintext)) {
            return "****";
        }
        int len = plaintext.length();
        if (len < 8) {
            return "****";
        }
        return plaintext.substring(0, 4) + "****" + plaintext.substring(len - 4);
    }

    /**
     * 判断是否已加密格式。
     *
     * @param value 待判断字符串
     * @return 是否已加密
     */
    @Override
    public boolean isEncrypted(String value) {
        return StrUtil.isNotEmpty(value) && value.startsWith(PREFIX_V1);
    }

    /**
     * 刷新当前生效密钥。
     *
     * @param base64Key 32 字节密钥的 Base64 编码
     */
    @Override
    public void refreshKey(String base64Key) {
        Assert.notBlank(base64Key, "encryptionKey 不能为空");
        byte[] keyBytes = Base64.decode(base64Key);
        Assert.isTrue(keyBytes.length == KEY_LENGTH, "encryptionKey 必须为 32 字节（Base64 解码后）");
        currentKey.set(new SecretKeySpec(keyBytes, "AES"));
    }

    /**
     * 确保密钥可用：优先用运行时刷新的密钥，其次用启动时配置。
     *
     * @return 密钥
     */
    private SecretKeySpec ensureKey() {
        SecretKeySpec key = currentKey.get();
        if (key != null) {
            return key;
        }
        if (StrUtil.isNotBlank(bootstrapKey)) {
            refreshKey(bootstrapKey);
            return currentKey.get();
        }
        throw new BusinessException("ENCRYPTION_KEY_NOT_INITIALIZED",
                "加密密钥未初始化，请在系统设置中配置 platform_basic.encryptionKey 或 application.yml 中配置 agentops.crypto.encryption-key");
    }
}
