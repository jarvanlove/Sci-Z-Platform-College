package com.sciz.server.application.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步任务
 * 
 * @author JiaWen.Wu
 * @className AsyncTask
 * @date 2025-10-29 10:00
 */
@Slf4j
@Component
public class AsyncTask {

    /**
     * 异步任务示例
     */
    @Async
    public void asyncTask() {
        log.info("执行异步任务");
        // TODO: 实现异步任务逻辑
    }
}
