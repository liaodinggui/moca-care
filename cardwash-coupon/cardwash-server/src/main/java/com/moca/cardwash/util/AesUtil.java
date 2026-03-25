package com.moca.cardwash.util;

import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * AES 加密工具类
 */
@Component
public class AesUtil {

    @Value("${qr.aes.key}")
    private String aesKey;

    /**
     * AES 加密
     */
    public String encrypt(String content) {
        AES aes = new AES(aesKey.getBytes(StandardCharsets.UTF_8));
        return aes.encryptHex(content);
    }

    /**
     * AES 解密
     */
    public String decrypt(String encryptedContent) {
        AES aes = new AES(aesKey.getBytes(StandardCharsets.UTF_8));
        return aes.decryptStr(encryptedContent);
    }
}
