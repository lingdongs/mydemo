package com.xxx.mybatisdemo.mapper;

import com.xxx.mybatisdemo.entity.JsonUser;
import org.apache.ibatis.annotations.Insert;

import java.util.List;
import java.util.Map;

public interface MapMapper {

    List<Map<String,String>> selectMap(String[] ids);

    List<Map> selectList();
    @Insert("insert into json_user (uid,data,created) values (#{uid},#{data},#{created})")
    int add(JsonUser jsonUser);

    int addBatch(List<JsonUser> list);
}
