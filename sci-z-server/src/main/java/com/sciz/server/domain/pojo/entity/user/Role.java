package com.sciz.server.domain.pojo.entity.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className Role
 * @date 2025-10-29 10:00
 */
@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
