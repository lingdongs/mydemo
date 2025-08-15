package com.xxx.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xxx.common.dto.TestParam;
import com.xxx.common.enums.CodesEnum;
import com.xxx.common.exception.CommonException;
import com.xxx.common.util.JsonUtil;
import com.xxx.common.util.RedisUtil;
import com.xxx.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController()
@RequestMapping("app/authed")
@Validated
public class TestController {

    @Value("${common.value}")
    private String test;

    @Resource
    private RedisUtil redisUtil;

    @PostMapping("post")
    public Result<?> log(@RequestHeader("Authorization") String authorization,
                         @RequestBody @Validated TestParam param) {

        redisUtil.set("k1", param,60);
        log.info(authorization);
        log.info(test);
        return Result.ok(param);
    }

    @GetMapping("get")
    public Result<?> log() {
        boolean login = StpUtil.isLogin();
        return Result.ok(login);
    }
}
