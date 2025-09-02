package com.xxx.util;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;
import java.util.stream.Collectors;

public class TreeMapTets {
    @Test
    public void test() {
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put("token", "0000dadklasamzmvv");
        paramMap.put("appid", "12346");
        paramMap.put("encryptType", "1");
        String str = paramMap.entrySet().stream().map(entry -> entry.getKey() + entry.getValue())
                .collect(Collectors.joining());
        System.out.println(str);
    }
}
