package com.xxx.mybatisdemo.controller;


import com.xxx.mybatisdemo.service.SWService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@RestController
public class SHIWUController {
    @Value("${common.test}")
    private  String testvlaue;
    @Resource
    private SWService swService;

    @RequestMapping("shiwu")
    @Transactional(rollbackFor = Exception.class)
    public String sw() {
            swService.sw();
            swService.sw2();
        return "成功";
    }

    @PostConstruct
    public void init() {
        System.out.println("testvlaue是"+testvlaue);
    }
}
