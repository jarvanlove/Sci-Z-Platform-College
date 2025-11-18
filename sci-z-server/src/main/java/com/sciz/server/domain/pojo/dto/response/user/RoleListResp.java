package com.sciz.server.domain.pojo.dto.response.user;

import java.time.LocalDateTime;

/**
 * 角色列表响应
 *
 * @param id          Long 角色ID
 * @param roleName    String 角色名称
 * @param roleCode    String 角色编码
 * @param roleType    String 角色类型
 * @param description String 角色描述
 * @param userCount   Integer 用户数量（绑定该角色的用户总数）
 * @param status      Integer 角色状态
 * @param sortOrder   Integer 排序值
 * @param createdTime LocalDateTime 创建时间
 * @param updatedTime LocalDateTime 更新时间
 * @author JiaWen.Wu
 * @className RoleListResp
 * @date 2025-11-14 17:00
 */
public record RoleListResp(
        Long id,
        String roleName,
        String roleCode,
        String roleType,
        String description,
        Integer userCount,
        Integer status,
        Integer sortOrder,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
