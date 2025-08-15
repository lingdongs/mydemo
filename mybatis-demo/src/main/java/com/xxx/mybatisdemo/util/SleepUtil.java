package com.xxx.mybatisdemo.util;

import java.util.concurrent.TimeUnit;

/**
 * @author ling
 */
public class SleepUtil {

    public static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }
}
