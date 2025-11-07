package com.sciz.server.application.service.user;

import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;

/**
 * 登录日志和统计服务
 * 在同一事务中保存登录日志并更新用户统计信息
 *
 * @author JiaWen.Wu
 * @className LoginLogAndStatsService
 * @date 2025-11-07 10:30
 */
public interface LoginLogAndStatsService {

    /**
     * 保存登录日志并更新用户统计（在同一事务中）
     *
     * @param event 登录日志事件
     */
    void saveLoginLogAndUpdateStats(LoginLoggedEvent event);
}
