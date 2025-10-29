package com.sciz.server.infrastructure.config.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步事件配置
 * 配置事件处理的线程池
 *
 * @author JiaWen.Wu
 * @className AsyncEventConfig
 * @date 2025-10-29 10:30
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncEventConfig {

    /**
     * 事件处理线程池
     *
     * @return 线程池执行器
     */
    @Bean("eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(5);

        // 最大线程数
        executor.setMaxPoolSize(20);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("event-handler-");

        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        // 初始化
        executor.initialize();

        log.info("事件处理线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
