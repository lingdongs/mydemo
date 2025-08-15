package com.xxx.mybatisdemo.controller;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.xxx.mybatisdemo.common.CodesEnum;
import com.xxx.mybatisdemo.common.CommonException;
import com.xxx.mybatisdemo.entity.JsonUser;
import com.xxx.mybatisdemo.entity.TimeDTO;
import com.xxx.mybatisdemo.entity.TimeVO;
import com.xxx.mybatisdemo.service.UserService;
import com.xxx.mybatisdemo.util.Result;
import com.xxx.mybatisdemo.util.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/get_user/{id}")
    public Result<?> getUser(@PathVariable("id") int id) {
        Map jsonObject = userService.getUserById(id);
        if (null == jsonObject) {
            throw new CommonException(CodesEnum.FAIL);
        }
        Map data = (Map) jsonObject.get("data");
        String name = String.valueOf(data.get("name"));
        Object time = jsonObject.get("created");
        log.info("time的class类型是{}", time.getClass());
        return ResultResponse.success(jsonObject);
    }


    @PostMapping("get")
    public Result<?> get(@RequestParam("id") int id) {
        JsonUser jsonUser = userService.get(id);
        Map<String, DataSource> beans = SpringUtil.getApplicationContext().getBeansOfType(DataSource.class);
        ClassUtils.isCglibProxy(beans.values().stream().findFirst().get());
        return ResultResponse.success(jsonUser);
    }

    @PostMapping("insert")
    public boolean insert(@RequestBody JsonUser jsonUser) {
     return userService.insert(jsonUser);
    }
    @PostMapping("get_time")
    public Result<?> getTime(@RequestBody TimeDTO timeDTO) {
        log.info("前端入参是{}", timeDTO);
        TimeVO timeVO = new TimeVO();
        timeVO.setDateTime(LocalDateTime.now());
        timeVO.setDate(new Date());
        log.info("出参是{}", timeVO);
        return ResultResponse.success(timeVO);
    }
}
