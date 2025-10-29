package com.sciz.server.domain.pojo.entity.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className User
 * @date 2025-10-29 10:00
 */
@Data
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
