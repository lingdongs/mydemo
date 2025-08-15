package com.xxx.mybatisdemo.util;

import com.xxx.mybatisdemo.common.CodesEnum;

public class ResultResponse {
    public static <T> Result<T> success(T data) {
        return Result.of(CodesEnum.SUCCESS.getCode(), CodesEnum.SUCCESS.getRemark(), data);
    }

    public static <T> Result<T> fail() {
        return Result.of(CodesEnum.FAIL.getCode(), CodesEnum.FAIL.getRemark(), null);
    }

    public static <T> Result<T> getResult(int code, String message, T data) {
        return Result.of(code, message, data);
    }
}
