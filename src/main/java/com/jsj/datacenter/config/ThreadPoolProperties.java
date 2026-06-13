package com.jsj.datacenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "threadpool")
@Data
public class ThreadPoolProperties {
    private int corePoolSize = 10;
    private int maxPoolSize = 20;
    private int queueCapacity = 50;
    private String threadNamePrefix = "MyExecutor-";
}