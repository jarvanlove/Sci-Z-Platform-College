package com.sciz.server.domain.pojo.dto.response.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className UserInfoResp
 * @date 2025-01-27 10:00
 */
@Data
public class UserInfoResp {
    private Long id;
    private String username;
    private String email;
    private String realName;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
}
