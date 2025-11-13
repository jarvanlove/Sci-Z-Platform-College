package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 创建用户响应
 *
 * @param id           Long 用户ID
 * @param username     String 用户名
 * @param realName     String 真实姓名
 * @param email        String 邮箱
 * @param phone        String 手机号
 * @param employeeId   String 学工号（自动生成）
 * @param departmentId Long 部门ID
 * @param industryType String 行业类型
 *
 * @author JiaWen.Wu
 * @className UserCreateResp
 * @date 2025-11-13 20:00
 */
public record UserCreateResp(
        Long id,
        String username,
        String realName,
        String email,
        String phone,
        String employeeId,
        Long departmentId,
        String industryType) {
}
