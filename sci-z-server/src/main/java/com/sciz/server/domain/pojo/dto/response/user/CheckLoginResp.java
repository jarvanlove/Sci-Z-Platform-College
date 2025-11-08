package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 登录状态校验响应
 *
 * @param login      boolean 是否已登录
 * @param loginId    String 登录ID（未登录返回 null）
 * @param expiresIn  Long Token 剩余秒数（未登录返回 0）
 * @param tokenName  String Token 名称（未登录返回 null）
 * @param tokenValue String Token 值（未登录返回 null）
 * @author JiaWen.Wu
 * @className CheckLoginResp
 * @date 2025-11-09 03:40
 */
public record CheckLoginResp(
        boolean login,
        String loginId,
        Long expiresIn,
        String tokenName,
        String tokenValue) {
}
