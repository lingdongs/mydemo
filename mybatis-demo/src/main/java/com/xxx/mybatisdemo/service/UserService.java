package com.xxx.mybatisdemo.service;

import com.alibaba.fastjson.JSONObject;
import com.xxx.mybatisdemo.entity.JsonUser;

import java.util.Map;

public interface UserService {
    Map getUserById(int id);

    void foo();

    JsonUser get(int id);

    boolean insert(JsonUser jsonUser);
}
