package com.sciz.server.domain.pojo.dto.response.system;

import java.time.LocalDateTime;

/**
 * 角色响应
 *
 * @param id          Long 角色ID
 * @param roleName    String 角色名称
 * @param roleCode    String 角色编码
 * @param roleType    String 角色类型
 * @param description String 角色描述
 * @param status      Integer 角色状态
 * @param sortOrder   Integer 排序值
 * @param createdTime LocalDateTime 创建时间
 * @param updatedTime LocalDateTime 更新时间
 *
 * @author JiaWen.Wu
 * @className RoleResp
 * @date 2025-11-17 14:00
 */
public record RoleResp(
        Long id,
        String roleName,
        String roleCode,
        String roleType,
        String description,
        Integer status,
        Integer sortOrder,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
