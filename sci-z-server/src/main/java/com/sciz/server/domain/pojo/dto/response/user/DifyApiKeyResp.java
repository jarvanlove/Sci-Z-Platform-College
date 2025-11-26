package com.sciz.server.domain.pojo.dto.response.user;

/**
 * Dify API 密钥响应
 *
 * @param id          Long 主键ID
 * @param userId      Long 用户ID
 * @param keyType     String 密钥类型
 * @param resourceId  String 资源ID（知识库ID或工作流ID）
 * @param keyName     String 密钥名称
 * @param description String 密钥描述
 * @author JiaWen.Wu
 * @className DifyApiKeyResp
 * @date 2025-01-28 14:30
 */
public record DifyApiKeyResp(
        Long id,
        Long userId,
        String keyType,
        String resourceId,
        String keyName,
        String description) {
}
