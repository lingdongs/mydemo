package com.xxx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GeneralApplication {
    public static void main(String[] args) {
        SpringApplication.run(GeneralApplication.class, args);
    }
}
