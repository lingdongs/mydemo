package com.xxx.common.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class TestParam {

    private Integer id;
    @NotEmpty
    private String name;
}
