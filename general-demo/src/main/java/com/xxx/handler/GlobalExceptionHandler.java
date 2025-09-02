package com.xxx.handler;

import com.xxx.common.enums.CodesEnum;
import com.xxx.common.exception.CommonException;
import com.xxx.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(CommonException.class)
    public Result<?> handleCommonException(CommonException e, HttpServletRequest request) {
        log.error("请求路径：{}，业务异常：{}", request.getRequestURI(), ExceptionUtils.getStackTrace(e));
        return new Result<>(e.getCodesEnum().getCode(), e.getCodesEnum().getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("请求路径：{}，参数异常：{}", request.getRequestURI(), ExceptionUtils.getStackTrace(e));
        return new Result<>(CodesEnum.PARAM_EX.getCode(), CodesEnum.PARAM_EX.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.error("请求路径：{}，参数异常：{}", request.getRequestURI(), ExceptionUtils.getStackTrace(e));
        return new Result<>(CodesEnum.PARAM_EX.getCode(), CodesEnum.PARAM_EX.getMessage());
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllExceptions(Exception e, HttpServletRequest request) {
        log.error("请求路径：{}，系统异常：{}", request.getRequestURI(), ExceptionUtils.getStackTrace(e));
        return new Result<>(CodesEnum.SYSTEM_EX.getCode(), CodesEnum.SYSTEM_EX.getMessage());
    }
}
