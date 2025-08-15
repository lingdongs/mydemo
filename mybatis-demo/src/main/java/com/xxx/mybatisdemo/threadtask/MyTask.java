package com.xxx.mybatisdemo.threadtask;

import com.xxx.mybatisdemo.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyTask {

    @Async("pool")
    public void task1() {
        log.info("{}线程正在运行", Thread.currentThread().getName());
        SleepUtil.sleep(10000);
        log.info("m1完成");
    }

    @Async("pool")
    public void task2() {
        SleepUtil.sleep(3000);
        log.info("m1完成");
    }

    @Async("pool")
    public void task3() {
        SleepUtil.sleep(3000);
        log.info("m1完成");
    }
}
