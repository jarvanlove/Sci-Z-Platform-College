package com.sciz.server.domain.pojo.dto.response.log;

import java.time.LocalDateTime;

/**
 * 登录日志响应
 *
 * @param id            Long 日志ID
 * @param userId        Long 用户ID
 * @param username      String 用户名
 * @param loginIp       String 登录IP
 * @param loginLocation String 登录地点
 * @param browser       String 浏览器
 * @param os            String 操作系统
 * @param status        Integer 登录状态（0失败 1成功）
 * @param message       String 提示消息
 * @param loginTime     LocalDateTime 登录时间
 * @author JiaWen.Wu
 * @className LoginLogResp
 * @date 2025-11-13 10:30
 */
public record LoginLogResp(
        Long id,
        Long userId,
        String username,
        String loginIp,
        String loginLocation,
        String browser,
        String os,
        Integer status,
        String message,
        LocalDateTime loginTime) {
}
