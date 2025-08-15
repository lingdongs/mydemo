package com.xxx.mybatisdemo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TimeDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
}
