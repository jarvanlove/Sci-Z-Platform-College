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
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final String KEY_NS = "sciz:auth:perm";
    private static final long TTL_SECONDS = 30L * 60L; // 30分钟

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

    @Override
    /**
     * 查询用户角色编码集合（含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 角色编码集合
     */
    public List<String> findRoleCodes(Long userId, String industryType) {
        String cacheKey = String.format("%s:%s:%s:roles", KEY_NS, userId, safe(industryType));
        String cached = RedisUtil.get(redis, cacheKey);
        if (cached != null) {
            return parseList(cached);
        }
        List<SysUserRole> urs = userRoleRepo.findNotDeletedByUserId(userId);
        if (CollectionUtils.isEmpty(urs)) {
            RedisUtil.set(redis, cacheKey, toCsv(Collections.emptyList()), java.time.Duration.ofSeconds(TTL_SECONDS));
            return Collections.emptyList();
        }
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).distinct().toList();
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<SysRole> roles = roleRepo.findByIds(roleIds);
        List<String> list = roles.stream()
                .filter(r -> r.getIsDeleted() == null || r.getIsDeleted() == 0)
                .filter(r -> industryType == null || industryType.equals(r.getIndustryType()))
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        RedisUtil.set(redis, cacheKey, toCsv(list), java.time.Duration.ofSeconds(TTL_SECONDS));
        return list;
    }

    @Override
    /**
     * 查询用户权限码集合（含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 权限码集合
     */
    public List<String> findPermissionCodes(Long userId, String industryType) {
        String cacheKey = String.format("%s:%s:%s:permissions", KEY_NS, userId, safe(industryType));
        String cached = RedisUtil.get(redis, cacheKey);
        if (cached != null) {
            return parseList(cached);
        }
        List<Long> roleIds = userRoleRepo.findNotDeletedByUserId(userId)
                .stream().map(SysUserRole::getRoleId).distinct().toList();
        if (CollectionUtils.isEmpty(roleIds)) {
            RedisUtil.set(redis, cacheKey, toCsv(Collections.emptyList()), java.time.Duration.ofSeconds(TTL_SECONDS));
            return Collections.emptyList();
        }

        List<Long> permIds = rolePermissionRepo.findNotDeletedByRoleIds(roleIds)
                .stream().map(SysRolePermission::getPermissionId).distinct().toList();
        if (CollectionUtils.isEmpty(permIds))
            return Collections.emptyList();

        List<SysPermission> perms = permissionRepo.findByIds(permIds);
        List<String> list = perms.stream()
                .filter(p -> p.getIsDeleted() == null || p.getIsDeleted() == 0)
                .filter(p -> industryType == null || industryType.equals(p.getIndustryType()))
                .map(SysPermission::getPermissionCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        RedisUtil.set(redis, cacheKey, toCsv(list), java.time.Duration.ofSeconds(TTL_SECONDS));
        return list;
    }

    @Override
    /**
     * 组装用户菜单树（基于菜单型权限，含行业过滤与缓存）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<LoginMenuResp> 菜单树
     */
    public List<LoginMenuResp> buildMenus(Long userId, String industryType) {
        String cacheKey = String.format("%s:%s:%s:menus", KEY_NS, userId, safe(industryType));
        String cached = RedisUtil.get(redis, cacheKey);
        if (cached != null) {
            return JsonUtil.fromJsonList(cached, LoginMenuResp.class);
        }
        // 复用上面取到的 permIds 构建菜单（permission_type = 1）
        List<Long> roleIds = userRoleRepo.findNotDeletedByUserId(userId)
                .stream().map(SysUserRole::getRoleId).distinct().toList();
        if (CollectionUtils.isEmpty(roleIds)) {
            RedisUtil.set(redis, cacheKey, JsonUtil.toJson(Collections.emptyList()),
                    java.time.Duration.ofSeconds(TTL_SECONDS));
            return Collections.emptyList();
        }

        List<Long> permIds = rolePermissionRepo.findNotDeletedByRoleIds(roleIds)
                .stream().map(SysRolePermission::getPermissionId).distinct().toList();
        if (CollectionUtils.isEmpty(permIds))
            return Collections.emptyList();

        List<SysPermission> perms = permissionRepo.findByIds(permIds);
        List<SysPermission> menuPerms = perms.stream()
                .filter(p -> p.getIsDeleted() == null || p.getIsDeleted() == 0)
                .filter(p -> industryType == null || industryType.equals(p.getIndustryType()))
                .filter(p -> p.getPermissionType() != null && p.getPermissionType() == 1)
                .collect(Collectors.toList());

        List<LoginMenuResp> menus = buildMenuTree(menuPerms);
        RedisUtil.set(redis, cacheKey, JsonUtil.toJson(menus), java.time.Duration.ofSeconds(TTL_SECONDS));
        return menus;
    }

    /**
     * 刷新指定用户在指定行业下的权限聚合缓存（角色/权限/菜单）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return void
     */
    @Override
    public void refreshUserAuthCache(Long userId, String industryType) {
        RedisUtil.delete(redis, String.format("%s:%s:%s:roles", KEY_NS, userId, safe(industryType)));
        RedisUtil.delete(redis, String.format("%s:%s:%s:permissions", KEY_NS, userId, safe(industryType)));
        RedisUtil.delete(redis, String.format("%s:%s:%s:menus", KEY_NS, userId, safe(industryType)));
    }

    private List<LoginMenuResp> buildMenuTree(List<SysPermission> menuPerms) {
        Map<Long, LoginMenuResp> id2node = new HashMap<>();
        List<LoginMenuResp> roots = new ArrayList<>();
        for (SysPermission p : menuPerms) {
            LoginMenuResp node = toNode(p);
            id2node.put(p.getId(), node);
        }
        for (SysPermission p : menuPerms) {
            Long pid = p.getParentId();
            LoginMenuResp node = id2node.get(p.getId());
            if (pid == null || pid == 0 || !id2node.containsKey(pid)) {
                roots.add(node);
            } else {
                LoginMenuResp parent = id2node.get(pid);
                if (parent.getChildren() == null)
                    parent.setChildren(new ArrayList<>());
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private LoginMenuResp toNode(SysPermission p) {
        LoginMenuResp n = new LoginMenuResp();
        n.setPath(p.getPath());
        n.setTitle(p.getPermissionName());
        n.setIcon(p.getIcon());
        n.setPermission(p.getPermissionCode());
        return n;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private List<String> parseList(String csv) {
        if (csv == null || csv.isEmpty())
            return Collections.emptyList();
        return Arrays.stream(csv.split(",")).filter(it -> !it.isEmpty()).distinct().toList();
    }

    private String toCsv(List<String> list) {
        if (list == null || list.isEmpty())
            return "";
        return String.join(",", list);
    }

}
