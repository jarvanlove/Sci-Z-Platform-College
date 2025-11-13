package com.sciz.server.infrastructure.external.dify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sciz.server.infrastructure.external.dify.common.service.BaseServiceImpl;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.mapper.DifyApiKeyMapper;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dify API 密钥管理服务实现类
 *
 * @author shihang.shang
 * @className DifyApiKeyServiceImpl
 * @date 2025-01-28 12:30
 */
@Slf4j
@Service
public class DifyApiKeyServiceImpl extends BaseServiceImpl<DifyApiKeyMapper, DifyApiKey> implements DifyApiKeyService {

    @Override
    public String getApiKey(Long userId, String resourceId, String keyType) {
        try {
            QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("resource_id", resourceId)
                       .eq("key_type", keyType)
                       .eq("is_active", true);
            
            DifyApiKey apiKey = this.getOne(queryWrapper);
            
            if (apiKey != null) {
                log.debug("找到用户 {} 的 {} 密钥，资源ID: {}", userId, keyType, resourceId);
                return apiKey.getApiKey();
            }
            
            // 如果用户没有配置密钥，尝试使用系统默认密钥
            log.warn("用户 {} 没有配置 {} 密钥，资源ID: {}，尝试使用系统默认密钥", userId, keyType, resourceId);
            return getDefaultApiKey(keyType, resourceId);
            
        } catch (Exception e) {
            log.error("获取API密钥失败: userId={}, resourceId={}, keyType={}, error={}", 
                    userId, resourceId, keyType, e.getMessage(), e);
            throw new RuntimeException("获取API密钥失败: " + e.getMessage());
        }
    }

    @Override
    public DifyApiKey saveOrUpdateApiKey(Long userId, String resourceId, String keyType,
                                        String apiKey, String keyName, String description, String operator) {
        try {
            QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("resource_id", resourceId)
                       .eq("key_type", keyType)
                       .eq("is_active", true);
            
            DifyApiKey existingKey = this.getOne(queryWrapper);
            
            if (existingKey != null) {
                // 更新现有密钥
                existingKey.setApiKey(apiKey);
                existingKey.setKeyName(keyName);
                existingKey.setDescription(description);
                existingKey.setUpdatedBy(operator);
                this.updateById(existingKey);
                log.info("更新用户 {} 的 {} 密钥，资源ID: {}", userId, keyType, resourceId);
                return existingKey;
            } else {
                // 创建新密钥
                DifyApiKey newKey = new DifyApiKey();
                newKey.setUserId(userId);
                newKey.setResourceId(resourceId);
                newKey.setKeyType(keyType);
                newKey.setApiKey(apiKey);
                newKey.setKeyName(keyName);
                newKey.setDescription(description);
                newKey.setIsActive(true);
                newKey.setCreatedBy(operator);
                newKey.setUpdatedBy(operator);
                this.save(newKey);
                log.info("创建用户 {} 的 {} 密钥，资源ID: {}", userId, keyType, resourceId);
                return newKey;
            }
            
        } catch (Exception e) {
            log.error("保存API密钥失败: userId={}, resourceId={}, keyType={}, error={}", 
                    userId, resourceId, keyType, e.getMessage(), e);
            throw new RuntimeException("保存API密钥失败: " + e.getMessage());
        }
    }

    @Override
    public List<DifyApiKey> getUserApiKeys(Long userId) {
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_active", true)
                   .orderByDesc("created_time");
        return this.list(queryWrapper);
    }

    @Override
    public List<DifyApiKey> getUserApiKeysByType(Long userId, String keyType) {
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("key_type", keyType)
                   .eq("is_active", true)
                   .orderByDesc("created_time");
        return this.list(queryWrapper);
    }

    @Override
    public void deactivateApiKey(Long id, String operator) {
        try {
            DifyApiKey apiKey = this.getById(id);
            if (apiKey != null) {
                apiKey.setIsActive(false);
                apiKey.setUpdatedBy(operator);
                this.updateById(apiKey);
                log.info("停用API密钥: id={}, userId={}, resourceId={}", 
                        id, apiKey.getUserId(), apiKey.getResourceId());
            } else {
                throw new RuntimeException("API密钥不存在: " + id);
            }
        } catch (Exception e) {
            log.error("停用API密钥失败: id={}, error={}", id, e.getMessage(), e);
            throw new RuntimeException("停用API密钥失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统默认API密钥
     *
     * @param keyType 密钥类型
     * @param resourceId 资源ID
     * @return API密钥
     */
    private String getDefaultApiKey(String keyType, String resourceId) {
        QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("key_type", keyType)
                   .eq("resource_id", resourceId)
                   .eq("is_active", true)
                   .orderByAsc("created_time");
        
        DifyApiKey defaultKey = this.getOne(queryWrapper);
        
        if (defaultKey != null) {
            log.debug("使用系统默认 {} 密钥，资源ID: {}", keyType, resourceId);
            return defaultKey.getApiKey();
        }
        
        throw new RuntimeException("未找到 " + keyType + " 的API密钥，资源ID: " + resourceId);
    }
}
