package com.jsj.datacenter.config;

import com.jsj.datacenter.config.ThreadPoolConfig;
import com.jsj.datacenter.config.ThreadPoolProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadPoolConfigTest {

    private ThreadPoolConfig threadPoolConfig;
    private ThreadPoolProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ThreadPoolProperties();
        properties.setCorePoolSize(5);
        properties.setMaxPoolSize(10);
        properties.setQueueCapacity(20);
        properties.setThreadNamePrefix("TestExecutor-");

        threadPoolConfig = new ThreadPoolConfig(properties);
    }

    @Test
    void testThreadPoolTaskExecutorBeanCreation() {
        // 测试线程池Bean的创建
        Executor executor = threadPoolConfig.threadPoolTaskExecutor();

        assertNotNull(executor, "线程池执行器不应该为null");
        assertTrue(executor instanceof ThreadPoolTaskExecutor, "执行器应该是ThreadPoolTaskExecutor类型");

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;

        // 验证配置值
        assertEquals(5, taskExecutor.getCorePoolSize(), "核心线程数配置错误");
        assertEquals(10, taskExecutor.getMaxPoolSize(), "最大线程数配置错误");
        assertEquals(20, taskExecutor.getQueueCapacity(), "队列容量配置错误");
        assertEquals("TestExecutor-", taskExecutor.getThreadNamePrefix(), "线程名前缀配置错误");
    }
}