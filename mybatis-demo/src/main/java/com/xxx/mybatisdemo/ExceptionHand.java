package com.xxx.mybatisdemo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHand {

    @ExceptionHandler(Exception.class)
    public String handleEx(Exception e ) {
        log.error(ExceptionUtils.getStackTrace(e));
        return "系统异常";
    }
}
