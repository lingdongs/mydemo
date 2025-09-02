package com.xxx.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Value("${common.value}")
    private String test;

    private final String test2 = new String(test.getBytes());


    public String test() {
        System.out.println(test2);
        return test2;
    }
}
