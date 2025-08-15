package com.xxx.mybatisdemo.controller;

import com.xxx.mybatisdemo.entity.JsonUser;
import com.xxx.mybatisdemo.mapper.MapMapper;
import com.xxx.mybatisdemo.util.MybatisBatchUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class MapController {

    @Resource
    private MapMapper mapMapper;
    @Resource
    private MybatisBatchUtils mybatisBatchUtils;

    @RequestMapping("map")
    public void m() {
        Map<String, Object> param = new HashMap<>();
        // String[] ids = {"1","2"};
        String[] ids = new String[0];
        List<Map<String, String>> res = mapMapper.selectMap(ids);
        return;
    }

    @RequestMapping("add")
    public void add() {
        List<JsonUser> list = new ArrayList<>(100_0000);
        for (int i = 0; i < 100_0000; i++) {
            JsonUser jsonUser = new JsonUser();
            jsonUser.setData(RandomStringUtils.randomAlphanumeric(6));
            jsonUser.setCreated(LocalDateTime.now());
            list.add(jsonUser);
        }
        long start = System.currentTimeMillis();
        // int row = mybatisBatchUtils.batchUpdateOrInsert(list, MapMapper.class, (jsonUser, mapper) -> mapper.add(jsonUser));
        int row = mapMapper.addBatch(list);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(row);
        return;
    }
}
