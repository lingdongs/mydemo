package com.xxx.mybatisdemo.service.impl;

import com.xxx.mybatisdemo.mapper.SWDao;
import com.xxx.mybatisdemo.service.SWService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class SWServiceImpl implements SWService {
    @Resource
    private SWDao swDao;

    @Override
    public void sw() {
        log.info("方法1执行了");

        Object me = AopContext.currentProxy();
        System.out.println(me);
        for (int i = 0; i < 5; i++) {
            insert();
        }
    }

    @Override
    public void sw2() {
        log.info("方法2执行了");
/*        SWService me = (SWService) AopContext.currentProxy();
        System.out.println(me);*/
        for (int i = 0; i < 1; i++) {
            insert();
        }
    }

    public void insert() {
        int insert = swDao.insert(RandomUtils.nextInt(10, 99), RandomStringUtils.randomAlphabetic(5));
        System.out.println(insert);
        if (RandomUtils.nextInt(0, 10) == 0) {
            throw new RuntimeException();
        }
    }
}
