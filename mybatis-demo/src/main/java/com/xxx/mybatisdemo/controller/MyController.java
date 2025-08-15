package com.xxx.mybatisdemo.controller;

import com.xxx.mybatisdemo.entity.JsonUser;
import com.xxx.mybatisdemo.threadtask.MyTask;
import com.xxx.mybatisdemo.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.Future;

@RestController
@Slf4j
@Validated
public class MyController {

    @Resource
    private MyTask myTask;

    @PostMapping("task")
    public void m1() {
        myTask.task1();
        // myTask.task2();
        // myTask.task3();
        log.info("完成");
    }

    @Async
    public Future<String> foo() {
        SleepUtil.sleep(3);
        return new AsyncResult<>("foo完成");
    }

    @GetMapping("test")
    public String m1(@RequestParam(value = "param")@Length(min = 2,max = 2) String param) {
        return String.valueOf(param);
    }

    @PostMapping("/test2")
    public String m2(@RequestBody @Validated JsonUser jsonUser) {
        return String.valueOf(jsonUser);
    }
}
