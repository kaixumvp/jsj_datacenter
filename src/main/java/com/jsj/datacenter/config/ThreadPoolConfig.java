package com.jsj.datacenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);
    private final ThreadPoolProperties properties;

    public ThreadPoolConfig(ThreadPoolProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        try {
            executor.setCorePoolSize(properties.getCorePoolSize()); // 核心线程数
            executor.setMaxPoolSize(properties.getMaxPoolSize()); // 最大线程数
            executor.setQueueCapacity(properties.getQueueCapacity()); // 队列容量
            executor.setThreadNamePrefix(properties.getThreadNamePrefix()); // 线程名前缀

            // 设置拒绝策略为记录日志并抛出异常
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

            // 设置线程池关闭时的策略
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(30);

            executor.initialize();
            logger.info("线程池配置初始化成功: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                       properties.getCorePoolSize(),
                       properties.getMaxPoolSize(),
                       properties.getQueueCapacity());
        } catch (Exception e) {
            logger.error("线程池配置初始化失败", e);
            throw new RuntimeException("线程池配置初始化失败", e);
        }
        return executor;
    }
}
