package com.sciz.server.application.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author JiaWen.Wu
 * @className ScheduledTask
 * @date 2025-10-29 10:00
 */
@Slf4j
@Component
public class ScheduledTask {

    /**
     * 定时任务示例
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledTask() {
        log.info("执行定时任务");
        // TODO: 实现定时任务逻辑
    }
}
