package com.sciz.server.infrastructure.external.redis.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Redis删除请求DTO
 *
 * @author JiaWen.Wu
 * @className RedisDeleteReq
 * @date 2025-10-29 10:30
 */
@Data
public class RedisDeleteReq {

    /**
     * 键
     */
    @NotBlank(message = "键不能为空")
    private String key;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 是否强制删除
     */
    private Boolean force = false;
}
