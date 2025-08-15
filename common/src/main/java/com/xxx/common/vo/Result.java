package com.xxx.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    private Result() {

    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "成功");
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "成功", data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(-1,"失败");
    }
}
