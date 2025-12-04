package com.sciz.server.domain.pojo.dto.request.project;

import jakarta.validation.constraints.NotNull;

/**
 * 项目成员更新请求
 *
 * @param userId Long 用户ID
 * @param role   String 角色（项目负责人、核心成员、普通成员）
 * @author JiaWen.Wu
 * @className ProjectMemberUpdateReq
 * @date 2025-12-01 10:00
 */
public record ProjectMemberUpdateReq(
        @NotNull(message = "用户ID不能为空") Long userId,
        String role) {
}
