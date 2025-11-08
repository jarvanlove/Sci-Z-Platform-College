package com.sciz.server.domain.pojo.dto.response.user;

import java.time.LocalDateTime;

/**
 * 刷新令牌响应
 *
 * @param loginId    String 登录ID
 * @param tokenName  String Token 名称
 * @param tokenValue String Token 值
 * @param expiresIn  Long Token 剩余秒数
 * @param expiresAt  LocalDateTime Token 过期时间（北京时间，可为空表示永不过期）
 * @author JiaWen.Wu
 * @className RefreshTokenResp
 * @date 2025-11-09 03:15
 */
public record RefreshTokenResp(
        String loginId,
        String tokenName,
        String tokenValue,
        Long expiresIn,
        LocalDateTime expiresAt) {
}
