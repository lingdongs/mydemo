package com.xxx.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class HmacTest {
    @Test
    public void test() {
        SecretKey key = SecureUtil.generateKey("HmacSHA256");
        System.out.println(HexUtil.encodeHexStr(key.getEncoded()));
        HMac hMac = SecureUtil.hmacSha256(key.getEncoded());
        String s = hMac.digestHex("123456");
        System.out.println(s.toUpperCase());
    }
}
