package com.xxx.mybatisdemo.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;


}
