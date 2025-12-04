package com.sciz.server.domain.pojo.dto.response.project;

import java.time.LocalDateTime;

/**
 * 项目成员响应
 *
 * @param id       Long 成员ID
 * @param userId   Long 用户ID
 * @param userName String 用户姓名
 * @param role     String 角色（项目负责人、核心成员、普通成员）
 * @param joinTime LocalDateTime 加入时间
 * @author JiaWen.Wu
 * @className ProjectMemberResp
 * @date 2025-12-01 09:18
 */
public record ProjectMemberResp(
        Long id,
        Long userId,
        String userName,
        String role,
        LocalDateTime joinTime) {
}
