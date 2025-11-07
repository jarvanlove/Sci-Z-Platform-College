package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 验证码响应
 *
 * @param captchaKey   String 验证码唯一标识（UUID），前端在登录时需要携带此 key
 * @param captchaImage String 验证码图片（Base64
 *                     编码），格式：data:image/png;base64,iVBORw0KGgo...
 * @param expiresIn    Long 验证码过期时间（秒）
 * @author JiaWen.Wu
 * @date 2025-11-07 16:00
 */
public record CaptchaResp(
        String captchaKey,
        String captchaImage,
        Long expiresIn) {
}
