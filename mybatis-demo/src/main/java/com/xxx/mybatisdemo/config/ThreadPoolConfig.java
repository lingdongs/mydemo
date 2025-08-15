package com.xxx.mybatisdemo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;


@Configuration
public class ThreadPoolConfig {

    // @Bean("pool")
    public Executor threadPoolExecutor() {
        return new ThreadPoolExecutor(5, 5,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}

