package com.xxx.mybatisdemo.service.impl;

import com.xxx.mybatisdemo.entity.JsonUser;
import com.xxx.mybatisdemo.mapper.UserMapper;
import com.xxx.mybatisdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class userServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Resource
    @Lazy
    private UserService userService;

    @Transactional
    public Map getUserById(int id) {
        userService.foo();
        UserService o = (UserService) AopContext.currentProxy();
        o.foo();

        Map map = userMapper.selectOneById(id);
        return map;

    }

    @Override
    public JsonUser get(int id) {

        return userMapper.getOne(id);
    }

    @Override
    public boolean insert(JsonUser jsonUser) {
        return userMapper.insert(jsonUser);
    }

    @Async
    public void foo() {
        log.info("foo async");
    }
}
