package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 更新用户响应
 *
 * @param id           Long 用户ID
 * @param username     String 用户名
 * @param realName     String 真实姓名
 * @param email        String 邮箱
 * @param phone        String 手机号
 * @param employeeId   String 学工号
 * @param departmentId Long 部门ID
 * @param industryType String 行业类型
 *
 * @author JiaWen.Wu
 * @className UserUpdateResp
 * @date 2025-11-13 22:00
 */
public record UserUpdateResp(
        Long id,
        String username,
        String realName,
        String email,
        String phone,
        String employeeId,
        Long departmentId,
        String industryType) {
}
