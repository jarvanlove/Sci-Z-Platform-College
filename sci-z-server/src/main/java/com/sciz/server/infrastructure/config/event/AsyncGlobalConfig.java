package com.sciz.server.infrastructure.config.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局异步线程池配置（与事件线程池并行存在）
 * 
 * @author JiaWen.Wu
 * @className AsyncGlobalConfig
 * @date 2025-10-29 17:00
 */
@Slf4j
@Configuration
public class AsyncGlobalConfig {

    /**
     * 全局任务执行器（通用异步任务，不用于事件监听）
     */
    @Bean("globalTaskExecutor")
    public Executor globalTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("global-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info(String.format("全局线程池初始化完成: core=%s, max=%s, queue=%s",
                String.valueOf(executor.getCorePoolSize()), String.valueOf(executor.getMaxPoolSize()),
                String.valueOf(executor.getThreadPoolExecutor().getQueue().remainingCapacity())));
        return executor;
    }
}
