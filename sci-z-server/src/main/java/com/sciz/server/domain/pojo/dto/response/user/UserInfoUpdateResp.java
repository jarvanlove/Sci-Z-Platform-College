package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 个人信息更新响应
 *
 * @param userInfo LoginUserInfoResp 用户信息
 *
 * @author JiaWen.Wu
 * @className UserInfoUpdateResp
 * @date 2025-11-12 18:30
 */
public record UserInfoUpdateResp(
        LoginUserInfoResp userInfo) {
}
