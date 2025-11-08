package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 用户注册响应
 *
 * @param userId   Long 用户ID
 * @param username String 用户名
 * @param realName String 真实姓名
 * @param email    String 邮箱
 * @param phone    String 手机号
 * @author JiaWen.Wu
 * @className RegisterResp
 * @date 2025-11-08 23:10
 */
public record RegisterResp(
        Long userId,
        String username,
        String realName,
        String email,
        String phone) {
}
