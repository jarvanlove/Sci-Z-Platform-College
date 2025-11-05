package com.sciz.server.infrastructure.external.redis.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Redis设置请求DTO
 *
 * @author JiaWen.Wu
 * @className RedisSetReq
 * @date 2025-10-29 10:30
 */
@Data
public class RedisSetReq {

    /**
     * 键
     */
    @NotBlank(message = "键不能为空")
    private String key;

    /**
     * 值
     */
    @NotBlank(message = "值不能为空")
    private String value;

    /**
     * 过期时间（秒）
     */
    private Long expireTime;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 是否覆盖
     */
    private Boolean overwrite = true;

    /**
     * 数据类型
     */
    private String dataType = "string";
}
