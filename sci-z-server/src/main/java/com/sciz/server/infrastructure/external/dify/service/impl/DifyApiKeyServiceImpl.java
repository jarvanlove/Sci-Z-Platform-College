package com.sciz.server.infrastructure.external.dify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sciz.server.infrastructure.external.dify.common.service.BaseServiceImpl;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.mapper.DifyApiKeyMapper;
import com.sciz.server.infrastructure.external.dify.service.DifyApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    /**
     * 根据用户ID、资源ID和密钥类型获取API密钥
     * 缓存 key: "dify:api-key:{userId}:{resourceId}:{keyType}"
     * TTL: 10 分钟（默认配置）
     *
     * @param userId     用户ID
     * @param resourceId 资源ID
     * @param keyType    密钥类型
     * @return API密钥
     */
    @Override
    @Cacheable(value = "dify:api-key", key = "#userId + ':' + (#resourceId != null ? #resourceId : 'null') + ':' + (#keyType != null ? #keyType : 'null')", unless = "#result == null")
    public String getApiKey(Long userId, String resourceId, String keyType) {
        try {
            LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DifyApiKey::getUserId, userId)
                    .eq(StringUtils.hasText(resourceId), DifyApiKey::getResourceId, resourceId)
                    .eq(StringUtils.hasText(keyType), DifyApiKey::getKeyType, keyType)
                    .eq(DifyApiKey::getIsActive, true);

            DifyApiKey apiKey = this.getOne(queryWrapper);

            if (apiKey != null) {
                log.debug("找到用户 {} 的 {} 密钥，资源ID: {}", userId, keyType, resourceId);
                return apiKey.getApiKey();
            }

            // 如果用户没有配置密钥，尝试使用系统默认密钥
            log.warn("用户 {} 没有配置 {} 密钥，资源ID: {}，尝试使用系统默认密钥", userId, keyType, resourceId);
            return getDefaultApiKey(keyType, resourceId);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取API密钥失败: userId={}, resourceId={}, keyType={}, error={}",
                    userId, resourceId, keyType, e.getMessage(), e);
            throw new RuntimeException("获取API密钥失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新API密钥
     * 更新时清除相关缓存
     *
     * @param userId      用户ID
     * @param resourceId  资源ID
     * @param keyType     密钥类型
     * @param apiKey      API密钥
     * @param keyName     密钥名称
     * @param description 描述
     * @param operator    操作者
     * @return 保存的密钥信息
     */
    @Override
    @CacheEvict(value = "dify:api-key", key = "#userId + ':' + (#resourceId != null ? #resourceId : 'null') + ':' + (#keyType != null ? #keyType : 'null')")
    public DifyApiKey saveOrUpdateApiKey(Long userId, String resourceId, String keyType,
            String apiKey, String keyName, String description, String operator) {
        try {
            LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DifyApiKey::getUserId, userId)
                    .eq(StringUtils.hasText(resourceId), DifyApiKey::getResourceId, resourceId)
                    .eq(StringUtils.hasText(keyType), DifyApiKey::getKeyType, keyType)
                    .eq(DifyApiKey::getIsActive, true);

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
        LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DifyApiKey::getUserId, userId)
                .eq(DifyApiKey::getIsActive, true)
                .orderByDesc(DifyApiKey::getCreatedTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<DifyApiKey> getAllApiKeys() {
        LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DifyApiKey::getIsActive, true)
                .eq(DifyApiKey::getDeleted, 0)
                .eq(DifyApiKey::getKeyType, "workflow")
                .orderByDesc(DifyApiKey::getCreatedTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<DifyApiKey> getUserApiKeysByType(Long userId, String keyType) {
        LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DifyApiKey::getUserId, userId)
                .eq(StringUtils.hasText(keyType), DifyApiKey::getKeyType, keyType)
                .eq(DifyApiKey::getIsActive, true)
                .orderByDesc(DifyApiKey::getCreatedTime);
        return this.list(queryWrapper);
    }

    /**
     * 停用API密钥
     * 停用时清除相关缓存
     *
     * @param id       密钥ID
     * @param operator 操作者
     */
    @Override
    @CacheEvict(value = "dify:api-key", allEntries = true)
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

    @Override
    public List<DifyApiKey> getReportTypes() {
        try {
            QueryWrapper<DifyApiKey> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("key_type", DifyApiKey.KeyType.WORKFLOW.getCode())
                    .like("key_name", "报告")
                    .eq("is_active", true)
                    .orderByDesc("created_time");

            List<DifyApiKey> reportTypes = this.list(queryWrapper);
            log.info(String.format("查询报告类型列表成功: 数量=%d", reportTypes.size()));
            return reportTypes;
        } catch (Exception e) {
            log.error(String.format("查询报告类型列表失败: err=%s", e.getMessage()), e);
            throw new RuntimeException("查询报告类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统默认API密钥
     *
     * @param keyType    密钥类型
     * @param resourceId 资源ID
     * @return API密钥
     */
    private String getDefaultApiKey(String keyType, String resourceId) {
        LambdaQueryWrapper<DifyApiKey> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(keyType), DifyApiKey::getKeyType, keyType)
                .eq(StringUtils.hasText(resourceId), DifyApiKey::getResourceId, resourceId)
                .eq(DifyApiKey::getIsActive, true)
                .orderByAsc(DifyApiKey::getCreatedTime);

        DifyApiKey defaultKey = this.getOne(queryWrapper);

        if (defaultKey != null) {
            log.debug("使用系统默认 {} 密钥，资源ID: {}", keyType, resourceId);
            return defaultKey.getApiKey();
        }

        throw new RuntimeException("未找到 " + keyType + " 的API密钥，资源ID: " + resourceId);
    }
}
