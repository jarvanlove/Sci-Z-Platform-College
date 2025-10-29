package com.sciz.server.infrastructure.external.redis.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Redis删除响应DTO
 *
 * @author JiaWen.Wu
 * @className RedisDeleteResp
 * @date 2025-10-29 10:30
 */
@Data
public class RedisDeleteResp {

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
     * 删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * 删除数量
     */
    private Long deletedCount;
}
