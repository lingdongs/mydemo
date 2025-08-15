package com.xxx.common.exception;

import com.xxx.common.enums.CodesEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
public class CommonException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private CodesEnum codesEnum;
}
