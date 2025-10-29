package com.sciz.server.infrastructure.external.redis.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Redis设置响应DTO
 *
 * @author JiaWen.Wu
 * @className RedisSetResp
 * @date 2025-10-29 10:30
 */
@Data
public class RedisSetResp {

    /**
     * 键
     */
    private String key;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 设置时间
     */
    private LocalDateTime setTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * TTL（秒）
     */
    private Long ttl;
}
