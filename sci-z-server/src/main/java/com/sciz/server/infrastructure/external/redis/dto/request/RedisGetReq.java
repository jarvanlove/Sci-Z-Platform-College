package com.sciz.server.infrastructure.external.redis.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Redis获取请求DTO
 *
 * @author JiaWen.Wu
 * @className RedisGetReq
 * @date 2025-10-29 10:30
 */
@Data
public class RedisGetReq {

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
     * 数据类型
     */
    private String dataType = "string";

    /**
     * 是否刷新过期时间
     */
    private Boolean refreshExpire = false;
}
