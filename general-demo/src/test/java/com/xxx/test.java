package com.xxx;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class test {


    @Test
    public void test() {
        long between = ChronoUnit.DAYS.between(LocalDate.parse("2025-05-14"), LocalDate.now());
        System.out.println(between);
    }

}
