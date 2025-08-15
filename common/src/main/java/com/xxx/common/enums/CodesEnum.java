package com.xxx.common.enums;

import lombok.Getter;

@Getter
public enum CodesEnum {

    SUCCESS(0, "成功"),
    FAIL(-1, "失败"),
    SYSTEM_EX(-2,"系统异常"),
    PARAM_EX(5001,"入参异常"),
    JSON_EX(5002,"JSON异常"),
    LOGIN_UN(5009, "登录过期"),
    ;

    private final Integer code;
    private final String message;

    CodesEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "CodesEnum{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
