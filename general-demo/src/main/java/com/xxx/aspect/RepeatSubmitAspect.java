package com.xxx.aspect;

import com.xxx.annotation.RepeatSubmit;
import com.xxx.common.enums.CodesEnum;
import com.xxx.common.exception.CommonException;
import com.xxx.common.util.RedisUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Cline
 * @date 2024-08-16
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    public static final String REPEAT_SUBMIT_KEY_PREFIX = "repeat_submit_token:";
    public static final String REPEAT_SUBMIT_HEADER = "X-Repeat-Token";

    @Resource
    private RedisUtil redisUtil;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        String token = request.getHeader(REPEAT_SUBMIT_HEADER);
        if (!StringUtils.hasText(token)) {
            throw new CommonException(CodesEnum.INVALID_TOKEN);
        }

        String key = REPEAT_SUBMIT_KEY_PREFIX + token;

        // Use Redis 'delete' command which returns a boolean.
        if (redisUtil.delete(key)) {
            // Token was valid and has been consumed. Proceed with the method execution.
            return joinPoint.proceed();
        } else {
            // Token is invalid or has already been used.
            throw new CommonException(CodesEnum.REPEAT_SUBMIT);
        }
    }
}
