package com.xxx.mybatisdemo.common;

public class CommonException extends RuntimeException {

    private CodesEnum codesEnum;
    private Integer code;

    public CommonException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public CommonException(CodesEnum codesEnum) {
        this.codesEnum = codesEnum;
    }

    public CodesEnum getCodesEnum() {
        return codesEnum;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "CommonException{" +
                "codesEnum=" + codesEnum +
                ", code=" + code +
                ", message=" + super.getMessage() +
                '}';
    }
}
