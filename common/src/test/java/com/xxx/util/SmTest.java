package com.xxx.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.symmetric.SM4;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;

public class SmTest {

    @Test
    public void testSm2() {
        String text = "我是一段测试aaaa";

        // 1. 生成 SM2 密钥对
        KeyPair pair = SecureUtil.generateKeyPair("SM2");
        BCECPrivateKey privateKey = (BCECPrivateKey) pair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) pair.getPublic();

        // 2. 获取原始私钥 (d) 和公钥 (x, y) 坐标
        String rawPrivateKey = privateKey.getD().toString(16);
        // 获取未压缩的公钥点，格式为 04 || x || y
        byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
        String rawPublicKey = HexUtil.encodeHexStr(publicKeyBytes);

        System.out.println("Raw Private Key (d): " + rawPrivateKey);
        System.out.println("Raw Public Key (04 || x || y): " + rawPublicKey);

        // 3. 使用标准 KeyPair 对象创建 SM2 实例以进行加密/解密和签名/验签
        // 这是最稳健的方式，可以避免手动处理字符串和编码格式引发的错误
        SM2 sm2 = SmUtil.sm2(rawPrivateKey,rawPublicKey);

        // 公钥加密
        String encryptStr = sm2.encryptHex(text, KeyType.PublicKey);
        System.out.println("加密后的文本 (Hex): " + encryptStr);

        // 私钥解密
        String decryptStr = sm2.decryptStr(encryptStr, KeyType.PrivateKey, CharsetUtil.CHARSET_UTF_8);
        System.out.println("解密后的文本: " + decryptStr);

        System.out.println("原文和解密后的文本是否一致: " + text.equals(decryptStr));

        // --- 签名和验签 ---
        System.out.println("\n--- 签名和验签 ---");
        // 获取原文的字节数组
        byte[] data = text.getBytes(CharsetUtil.CHARSET_UTF_8);

        // 签名，得到签名的字节数组
        byte[] signature = sm2.sign(data);
        System.out.println("生成的签名 (Hex): " + HexUtil.encodeHexStr(signature));

        // 验签
        boolean verify = sm2.verify(data, signature);
        System.out.println("验签结果: " + verify);
    }

    @Test
    public void testSm3() {
        String content = "test sm3";
        String hash = SmUtil.sm3(content);
        System.out.println("Original: " + content);
        System.out.println("SM3 Hash: " + hash);
    }

    @Test
    public void testHmacSm3() {
        String content = "test hmac-sm3 with key";
        // 生成一个随机的、安全的密钥
        // 也可以使用你自己的密钥字节数组：byte[] keyBytes = "your-secret-key".getBytes();
        SecretKey secretKey = SecureUtil.generateKey("HmacSM3");

        // 创建 HmacSM3 实例
        HMac hmac = SmUtil.hmacSm3(secretKey.getEncoded());

        // 对内容进行摘要
        String digest = hmac.digestHex(content);

        System.out.println("Original: " + content);
        System.out.println("HMAC-SM3 Hash: " + digest);
    }

    @Test
    public void testSm4() {
        String content = "test sm4";
        // 生成随机密钥
        byte[] key = SecureUtil.generateKey("SM4").getEncoded();

        // 创建 SM4 实例
        SM4 sm4 = SmUtil.sm4(key);

        // 加密
        String encryptHex = sm4.encryptHex(content);
        System.out.println("Original: " + content);
        System.out.println("SM4 Encrypted (Hex): " + encryptHex);

        // 解密
        String decryptStr = sm4.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        System.out.println("SM4 Decrypted: " + decryptStr);

        // 校验
        System.out.println("Is content same as decrypted string: " + content.equals(decryptStr));
    }
}
