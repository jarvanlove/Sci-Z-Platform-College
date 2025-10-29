package com.sciz.server.domain.pojo.entity.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Permission
 * @date 2025-10-29 10:00
 */
@Data
public class Permission {
    private Long id;
    private String permissionName;
    private String permissionCode;
    private String resource;
    private String action;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
