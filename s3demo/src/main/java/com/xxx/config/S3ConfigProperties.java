package com.xxx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "s3")
public class S3ConfigProperties {

    private String endpoint;
    private boolean testenv;
    private Temp temp;

    @Data
    public static class Temp {
        private String access_key_id;
        private String secret_key;
        private String qdztbucket;
    }
}
