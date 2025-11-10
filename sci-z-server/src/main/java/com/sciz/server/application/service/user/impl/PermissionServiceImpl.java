package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.domain.pojo.dto.response.user.LoginMenuResp;
import com.sciz.server.domain.pojo.entity.user.SysPermission;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysPermissionRepo;
import com.sciz.server.domain.pojo.repository.user.SysRolePermissionRepo;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.PermissionType;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 用户权限聚合查询实现
 *
 * @author JiaWen.Wu
 * @className PermissionServiceImpl
 * @date 2025-10-31 11:40
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysUserRoleRepo userRoleRepo;
    private final SysRoleRepo roleRepo;
    private final SysRolePermissionRepo rolePermissionRepo;
    private final SysPermissionRepo permissionRepo;
    private final StringRedisTemplate redis;

    public PermissionServiceImpl(SysUserRoleRepo userRoleRepo,
            SysRoleRepo roleRepo,
            SysRolePermissionRepo rolePermissionRepo,
            SysPermissionRepo permissionRepo,
            StringRedisTemplate redis) {
        this.userRoleRepo = userRoleRepo;
        this.roleRepo = roleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.permissionRepo = permissionRepo;
        this.redis = redis;
    }

    /**
     * 查询用户角色编码集合（含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 角色编码集合
     */
    @Override
    public List<String> findRoleCodes(Long userId, String industryType) {
        // 1. 检查缓存
        var cacheKey = buildCacheKey(userId, industryType, "roles");
        Optional<List<String>> cachedList = Optional.ofNullable(RedisUtil.get(redis, cacheKey))
                .map(this::parseList);
        if (cachedList.isPresent()) {
            return cachedList.get();
        }

        // 2. 查询用户角色关联
        var userRoles = userRoleRepo.findNotDeletedByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            cacheEmptyResult(cacheKey);
            return Collections.emptyList();
        }

        // 3. 提取角色ID列表
        var roleIds = extractRoleIds(userRoles);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        // 4. 查询角色并过滤
        var roles = roleRepo.findByIds(roleIds);
        var roleCodes = filterAndMapRoleCodes(roles, industryType);

        // 5. 缓存结果
        cacheResult(cacheKey, toCsv(roleCodes));
        return roleCodes;
    }

    /**
     * 查询用户权限码集合（含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 权限码集合
     */
    @Override
    public List<String> findPermissionCodes(Long userId, String industryType) {
        // 1. 检查缓存
        var cacheKey = buildCacheKey(userId, industryType, "permissions");
        Optional<List<String>> cachedList = Optional.ofNullable(RedisUtil.get(redis, cacheKey))
                .map(this::parseList);
        if (cachedList.isPresent()) {
            return cachedList.get();
        }

        // 2. 查询用户角色ID列表
        var roleIds = findUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            cacheEmptyResult(cacheKey);
            return Collections.emptyList();
        }

        // 3. 查询角色权限ID列表
        var permIds = findRolePermissionIds(roleIds);
        if (CollectionUtils.isEmpty(permIds)) {
            return Collections.emptyList();
        }

        // 4. 查询权限并过滤
        var perms = permissionRepo.findByIds(permIds);
        var permissionCodes = filterAndMapPermissionCodes(perms, industryType);

        // 5. 缓存结果
        cacheResult(cacheKey, toCsv(permissionCodes));
        return permissionCodes;
    }

    /**
     * 组装用户菜单树（基于菜单型权限，含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<LoginMenuResp> 菜单树
     */
    @Override
    public List<LoginMenuResp> buildMenus(Long userId, String industryType) {
        // 1. 检查缓存
        var cacheKey = buildCacheKey(userId, industryType, "menus");
        Optional<List<LoginMenuResp>> cachedMenus = Optional.ofNullable(RedisUtil.get(redis, cacheKey))
                .map(json -> JsonUtil.fromJsonList(json, LoginMenuResp.class));
        if (cachedMenus.isPresent()) {
            return cachedMenus.get();
        }

        // 2. 查询用户角色ID列表
        var roleIds = findUserRoleIds(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            var emptyMenus = Collections.<LoginMenuResp>emptyList();
            RedisUtil.set(redis, cacheKey, JsonUtil.toJson(emptyMenus),
                    Duration.ofSeconds(CacheConstant.PERMISSION_CACHE_EXPIRE));
            return emptyMenus;
        }

        // 3. 查询角色权限ID列表
        var permIds = findRolePermissionIds(roleIds);
        if (CollectionUtils.isEmpty(permIds)) {
            return Collections.emptyList();
        }

        // 4. 查询菜单类型的权限
        var perms = permissionRepo.findByIds(permIds);
        var menuPerms = filterMenuPermissions(perms, industryType);

        // 5. 构建菜单树并缓存
        var menus = buildMenuTree(menuPerms);
        RedisUtil.set(redis, cacheKey, JsonUtil.toJson(menus),
                Duration.ofSeconds(CacheConstant.PERMISSION_CACHE_EXPIRE));
        return menus;
    }

    /**
     * 获取用户主要角色编码（第一个角色）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return String 主要角色编码，如果没有角色则返回 null
     */
    @Override
    public String getPrimaryRoleCode(Long userId, String industryType) {
        var roleCodes = findRoleCodes(userId, industryType);
        return roleCodes.isEmpty() ? null : roleCodes.get(0);
    }

    /**
     * 刷新指定用户在指定行业下的权限聚合缓存（角色/权限/菜单）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     */
    @Override
    public void refreshUserAuthCache(Long userId, String industryType) {
        RedisUtil.delete(redis, buildCacheKey(userId, industryType, "roles"));
        RedisUtil.delete(redis, buildCacheKey(userId, industryType, "permissions"));
        RedisUtil.delete(redis, buildCacheKey(userId, industryType, "menus"));

        // 重新加载聚合信息，确保缓存立即生效
        findRoleCodes(userId, industryType);
        findPermissionCodes(userId, industryType);
        buildMenus(userId, industryType);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建缓存键
     *
     * @param userId       用户ID
     * @param industryType 行业类型
     * @param type         缓存类型（roles/permissions/menus）
     * @return 缓存键
     */
    private String buildCacheKey(Long userId, String industryType, String type) {
        return String.format("%s:%s:%s:%s",
                CacheConstant.PERMISSION_CACHE_NAMESPACE, userId, safe(industryType), type);
    }

    /**
     * 缓存空结果
     *
     * @param cacheKey 缓存键
     */
    private void cacheEmptyResult(String cacheKey) {
        RedisUtil.set(redis, cacheKey, toCsv(Collections.emptyList()),
                Duration.ofSeconds(CacheConstant.PERMISSION_CACHE_EXPIRE));
    }

    /**
     * 缓存结果
     *
     * @param cacheKey 缓存键
     * @param value    缓存值
     */
    private void cacheResult(String cacheKey, String value) {
        RedisUtil.set(redis, cacheKey, value,
                Duration.ofSeconds(CacheConstant.PERMISSION_CACHE_EXPIRE));
    }

    /**
     * 查询用户角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    private List<Long> findUserRoleIds(Long userId) {
        return userRoleRepo.findNotDeletedByUserId(userId)
                .stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
    }

    /**
     * 提取角色ID列表
     *
     * @param userRoles 用户角色关联列表
     * @return 角色ID列表
     */
    private List<Long> extractRoleIds(List<SysUserRole> userRoles) {
        return userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
    }

    /**
     * 查询角色权限ID列表
     *
     * @param roleIds 角色ID列表
     * @return 权限ID列表
     */
    private List<Long> findRolePermissionIds(List<Long> roleIds) {
        return rolePermissionRepo.findNotDeletedByRoleIds(roleIds)
                .stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .toList();
    }

    /**
     * 过滤并映射角色编码
     *
     * @param roles        角色列表
     * @param industryType 行业类型
     * @return 角色编码列表
     */
    private List<String> filterAndMapRoleCodes(List<SysRole> roles, String industryType) {
        return roles.stream()
                .filter(role -> !Optional.ofNullable(role.getIsDeleted())
                        .map(DeleteStatus.DELETED.getCode()::equals)
                        .orElse(false))
                .filter(role -> Optional.ofNullable(industryType)
                        .map(type -> type.equals(role.getIndustryType()))
                        .orElse(true))
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 过滤并映射权限编码
     *
     * @param perms        权限列表
     * @param industryType 行业类型
     * @return 权限编码列表
     */
    private List<String> filterAndMapPermissionCodes(List<SysPermission> perms, String industryType) {
        return perms.stream()
                .filter(perm -> !Optional.ofNullable(perm.getIsDeleted())
                        .map(DeleteStatus.DELETED.getCode()::equals)
                        .orElse(false))
                .filter(perm -> Optional.ofNullable(industryType)
                        .map(type -> type.equals(perm.getIndustryType()))
                        .orElse(true))
                .map(SysPermission::getPermissionCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 过滤菜单类型的权限
     *
     * @param perms        权限列表
     * @param industryType 行业类型
     * @return 菜单权限列表
     */
    private List<SysPermission> filterMenuPermissions(List<SysPermission> perms, String industryType) {
        return perms.stream()
                .filter(perm -> !Optional.ofNullable(perm.getIsDeleted())
                        .map(DeleteStatus.DELETED.getCode()::equals)
                        .orElse(false))
                .filter(perm -> Optional.ofNullable(industryType)
                        .map(type -> type.equals(perm.getIndustryType()))
                        .orElse(true))
                .filter(perm -> Optional.ofNullable(perm.getPermissionType())
                        .map(type -> PermissionType.MENU.getCode().equals(type))
                        .orElse(false))
                .toList();
    }

    /**
     * 构建菜单树形结构
     *
     * @param menuPerms 菜单权限列表
     * @return 菜单树
     */
    private List<LoginMenuResp> buildMenuTree(List<SysPermission> menuPerms) {
        var idToNode = new HashMap<Long, LoginMenuResp>();
        var roots = new ArrayList<LoginMenuResp>();

        // 第一遍：构建所有节点
        for (var perm : menuPerms) {
            var node = toNode(perm);
            idToNode.put(perm.getId(), node);
        }

        // 第二遍：构建树形结构
        for (var perm : menuPerms) {
            var parentId = perm.getParentId();
            var node = idToNode.get(perm.getId());
            if (Optional.ofNullable(parentId).filter(id -> id != 0 && idToNode.containsKey(id)).isEmpty()) {
                roots.add(node);
            } else {
                var parent = idToNode.get(parentId);
                parent.setChildren(Optional.ofNullable(parent.getChildren()).orElseGet(ArrayList::new));
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /**
     * 将权限实体转换为菜单节点
     *
     * @param perm 权限实体
     * @return 菜单节点
     */
    private LoginMenuResp toNode(SysPermission perm) {
        var node = new LoginMenuResp();
        node.setPath(perm.getPath());
        node.setTitle(perm.getPermissionName());
        node.setIcon(perm.getIcon());
        node.setPermission(perm.getPermissionCode());
        return node;
    }

    /**
     * 安全字符串处理（null 转为空字符串）
     *
     * @param str 字符串
     * @return 非 null 的字符串
     */
    private String safe(String str) {
        return Optional.ofNullable(str).orElse("");
    }

    /**
     * 解析 CSV 字符串为列表
     *
     * @param csv CSV 字符串
     * @return 字符串列表
     */
    private List<String> parseList(String csv) {
        return Optional.ofNullable(csv)
                .filter(value -> !value.isEmpty())
                .map(value -> Arrays.stream(value.split(","))
                        .filter(item -> !item.isEmpty())
                        .distinct()
                        .toList())
                .orElse(Collections.emptyList());
    }

    /**
     * 将列表转换为 CSV 字符串
     *
     * @param list 字符串列表
     * @return CSV 字符串
     */
    private String toCsv(List<String> list) {
        return Optional.ofNullable(list)
                .filter(items -> !items.isEmpty())
                .map(items -> String.join(",", items))
                .orElse("");
    }

}
