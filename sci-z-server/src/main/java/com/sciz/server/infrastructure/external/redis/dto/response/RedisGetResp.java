package com.sciz.server.infrastructure.external.redis.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Redis获取响应DTO
 *
 * @author JiaWen.Wu
 * @className RedisGetResp
 * @date 2025-10-29 10:30
 */
@Data
public class RedisGetResp {

    /**
     * 键
     */
    private String key;

    /**
     * 值
     */
    private String value;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 获取时间
     */
    private LocalDateTime getTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * TTL（秒）
     */
    private Long ttl;

    /**
     * 是否存在
     */
    private Boolean exists;
}
