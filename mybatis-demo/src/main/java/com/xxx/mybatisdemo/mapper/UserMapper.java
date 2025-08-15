package com.xxx.mybatisdemo.mapper;

import com.alibaba.fastjson.JSONObject;
import com.xxx.mybatisdemo.entity.JsonUser;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface UserMapper {

    // @Select("SELECT * FROM json_user where id = #{id}}")
    Map selectOneById(@Param("id") int id);

    JsonUser getOne(@Param("id") int id);

    boolean insert(JsonUser jsonUser);
}
