package com.sciz.server.domain.pojo.dto.response.user;

import java.time.LocalDateTime;

/**
 * 用户列表响应
 *
 * @param id              Long 用户ID
 * @param username        String 用户名
 * @param realName        String 真实姓名
 * @param employeeId      String 工号（学工号）
 * @param email           String 邮箱
 * @param phone           String 手机号
 * @param departmentId    Long 部门ID
 * @param departmentName  String 部门名称
 * @param departmentLabel String 部门标签（通用字段，不同行业叫法不同，如"院系"、"部门"等）
 * @param roleName        String 角色名称（主要角色）
 * @param status          Integer 用户状态（1=正常，0=禁用）
 * @param createTime      LocalDateTime 创建时间
 *
 * @author JiaWen.Wu
 * @className UserListResp
 * @date 2025-11-13 16:00
 */
public record UserListResp(
                Long id,
                String username,
                String realName,
                String employeeId,
                String email,
                String phone,
                Long departmentId,
                String departmentName,
                String departmentLabel,
                String roleName,
                Integer status,
                LocalDateTime createTime) {
}
