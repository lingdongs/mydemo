package com.xxx.mybatisdemo.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum CodesEnum {
    SUCCESS(0, "成功"),
    FAIL(-1, "失败"),

    PARAM_EXCEPTION(-2,"参数异常");
    private final Integer code;
    private final String remark;

}
