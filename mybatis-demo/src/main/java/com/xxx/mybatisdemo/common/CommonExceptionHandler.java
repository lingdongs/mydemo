package com.xxx.mybatisdemo.common;

import com.xxx.mybatisdemo.util.Result;
import com.xxx.mybatisdemo.util.ResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public Result<?> hadleCommonException(CommonException ce) {
        log.error("全局异常统一处理:自定义异常是{}", ExceptionUtils.getStackTrace(ce));
        if (null != ce.getCodesEnum()) {
            return ResultResponse.getResult(ce.getCodesEnum().getCode(), ce.getCodesEnum().getRemark(), null);
        }
        return ResultResponse.getResult(ce.getCode(), ce.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("全局异常统一处理:异常是{}", ExceptionUtils.getStackTrace(e));
        return ResultResponse.getResult(CodesEnum.FAIL.getCode(), CodesEnum.FAIL.getRemark(), null);
    }

    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class})
    public Result<?> handleParamException(Exception e) {
        log.error("全局异常统一处理:入参异常是{}", ExceptionUtils.getStackTrace(e));
        return ResultResponse.getResult(CodesEnum.PARAM_EXCEPTION.getCode(), CodesEnum.PARAM_EXCEPTION.getRemark(), ExceptionUtils.getStackTrace(e));
    }
}
