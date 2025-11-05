package com.sciz.server.infrastructure.external.dify.service;

import com.sciz.server.infrastructure.external.dify.common.service.BaseService;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;

import java.util.List;

/**
 * Dify API 密钥管理服务接口
 *
 * @author shihang.shang
 * @className DifyApiKeyService
 * @date 2025-01-28 12:30
 */
public interface DifyApiKeyService extends BaseService<DifyApiKey> {

    /**
     * 根据用户ID、资源ID和密钥类型获取API密钥
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @return API密钥
     */
    String getApiKey(String userId, String resourceId, String keyType);

    /**
     * 保存或更新API密钥
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param keyType 密钥类型
     * @param apiKey API密钥
     * @param keyName 密钥名称
     * @param description 描述
     * @param operator 操作者
     * @return 保存的密钥信息
     */
    DifyApiKey saveOrUpdateApiKey(String userId, String resourceId, String keyType, 
                                 String apiKey, String keyName, String description, String operator);

    /**
     * 获取用户的所有密钥
     *
     * @param userId 用户ID
     * @return 密钥列表
     */
    List<DifyApiKey> getUserApiKeys(String userId);

    /**
     * 获取用户指定类型的密钥
     *
     * @param userId 用户ID
     * @param keyType 密钥类型
     * @return 密钥列表
     */
    List<DifyApiKey> getUserApiKeysByType(String userId, String keyType);

    /**
     * 停用API密钥
     *
     * @param id 密钥ID
     * @param operator 操作者
     */
    void deactivateApiKey(Long id, String operator);
}