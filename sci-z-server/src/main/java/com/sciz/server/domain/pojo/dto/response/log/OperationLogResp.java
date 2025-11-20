package com.sciz.server.domain.pojo.dto.response.log;

import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import java.time.LocalDateTime;

/**
 * 操作日志响应
 *
 * @param id             Long 日志ID
 * @param userId         Long 用户ID
 * @param username       String 用户名
 * @param operation      String 操作名称
 * @param method         String 请求方法
 * @param requestUrl     String 请求URL
 * @param requestParams  String 请求参数（JSON格式）
 * @param responseResult String 响应结果（JSON格式）
 * @param ipAddress      String IP地址
 * @param location       String 操作地点
 * @param browser        String 浏览器
 * @param os             String 操作系统
 * @param logLevel       LogLevelStatus 日志级别
 * @param status         Integer 操作状态（0:失败,1:成功）
 * @param executionTime  Integer 执行耗时（毫秒）
 * @param detailInfo     String 详细信息
 * @param errorMessage   String 错误信息
 * @param operationTime  LocalDateTime 操作时间（创建时间）
 * @author JiaWen.Wu
 * @className OperationLogResp
 * @date 2025-11-14 20:30
 */
public record OperationLogResp(
                Long id,
                Long userId,
                String username,
                String operation,
                String method,
                String requestUrl,
                String requestParams,
                String responseResult,
                String ipAddress,
                String location,
                String browser,
                String os,
                LogLevelStatus logLevel,
                Integer status,
                Integer executionTime,
                String detailInfo,
                String errorMessage,
                LocalDateTime operationTime) {
}
