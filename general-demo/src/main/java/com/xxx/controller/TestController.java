package com.xxx.controller;

import com.xxx.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xxx.service.TestService;

import javax.annotation.Resource;


@Slf4j
@RestController()
@Validated
public class TestController {

    @Value("${common.value}")
    private String test;

    @Resource
    private TestService testService;

    @GetMapping("test")
    public Result test() {
        return Result.ok(testService.test());
    }
}
