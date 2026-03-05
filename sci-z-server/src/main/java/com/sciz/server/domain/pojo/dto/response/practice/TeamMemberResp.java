package com.sciz.server.domain.pojo.dto.response.practice;

/**
 * 产教研智能体 - 团队成员项（含学院、职务，用于项目负责人/成员展示）
 *
 * @param userId   用户ID
 * @param userName 用户姓名（展示用，与 realName 二选一）
 * @param realName 真实姓名
 * @param college  学院/部门名称
 * @param position 职务（可与 role 一致，如负责人、普通用户）
 * @param role     角色
 */
public record TeamMemberResp(
        Long userId,
        String userName,
        String realName,
        String college,
        String position,
        String role) {

    /** 兼容旧版：仅 userName、role 时 college/position 为 null */
    public static TeamMemberResp of(Long userId, String userName, String role, String college, String position) {
        return new TeamMemberResp(userId, userName, userName, college, position != null ? position : role, role);
    }
}
