package com.xxx.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xxx.annotation.RepeatSubmit;
import com.xxx.aspect.RepeatSubmitAspect;
import com.xxx.common.dto.TestParam;
import com.xxx.common.util.RedisUtil;
import com.xxx.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;


@Slf4j
@RestController()
@RequestMapping("app/authed")
@Validated
public class TestController {

    @Value("${common.value}")
    private String test;

    @Resource
    private RedisUtil redisUtil;

    @RepeatSubmit
    @PostMapping("post")
    public Result<?> log(@RequestBody @Validated TestParam param) {
        // The @RepeatSubmit aspect now handles the token, so we can remove the authorization logic from here.
        log.info("Received param: {}", param);
        log.info("Common value from config: {}", test);
        return Result.ok(param);
    }

    @GetMapping("get")
    public Result<?> log() {
        boolean login = StpUtil.isLogin();
        return Result.ok(login);
    }

    /**
     * 获取防重提交令牌
     * @return
     */
    @GetMapping("token")
    public Result<String> getToken() {
        String token = UUID.randomUUID().toString();
        String key = RepeatSubmitAspect.REPEAT_SUBMIT_KEY_PREFIX + token;
        // Store the token in Redis with a 5-minute expiration time.
        redisUtil.set(key, "1", 300);
        return Result.ok(token);
    }
}
