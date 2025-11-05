package com.sciz.server.application.service.user.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserInfoResp;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 认证应用服务实现
 *
 * @author JiaWen.Wu
 * @className AuthServiceImpl
 * @date 2025-10-30 11:10
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String AUTH_FAIL_KEY = "auth:fail:%s";
    private static final String AUTH_LOCK_KEY = "auth:lock:%s";
    private static final int MAX_FAIL = 6;
    private static final long LOCK_SECONDS = 30L * 60L;

    private final SysUserRepo sysUserRepo;
    private final StringRedisTemplate stringRedisTemplate;
    private final EventPublisher eventPublisher;
    private final PermissionService permissionService;
    private final IndustryConfigCache industryConfigCache;
    private final SysDepartmentRepo sysDepartmentRepo;

    public AuthServiceImpl(SysUserRepo sysUserRepo, StringRedisTemplate stringRedisTemplate,
            EventPublisher eventPublisher,
            com.sciz.server.application.service.user.PermissionService permissionService,
            IndustryConfigCache industryConfigCache,
            SysDepartmentRepo sysDepartmentRepo) {
        this.sysUserRepo = sysUserRepo;
        this.stringRedisTemplate = stringRedisTemplate;
        this.eventPublisher = eventPublisher;
        this.permissionService = permissionService;
        this.industryConfigCache = industryConfigCache;
        this.sysDepartmentRepo = sysDepartmentRepo;
    }

    /**
     * 用户登录（账号锁定校验 → 用户有效性校验 → 密码校验 → 清理失败计数 → Sa-Token 登录 → 组装返回 → 发布事件）
     *
     * @param username    String 用户名
     * @param rawPassword String 明文密码
     * @param rememberMe  Boolean 记住我（持久化 Cookie 与较长超时）
     * @return LoginResp 登录返回（包含 token、用户信息、角色/权限/菜单）
     */
    @Override
    public LoginResp login(String username, String rawPassword, Boolean rememberMe) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            log.warn(String.format("login bad request, username=%s", username));
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        // 锁定校验
        String lockKey = String.format(AUTH_LOCK_KEY, username);
        String locked = RedisUtil.get(stringRedisTemplate, lockKey);
        if (StringUtils.hasText(locked)) {
            log.warn(String.format("login locked, username=%s", username));
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已锁定，请稍后再试");
        }

        log.info(String.format("login start, username=%s", username));
        SysUser user = sysUserRepo.findByUsername(username);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            onFail(username);
            log.warn(String.format("login user invalid, username=%s", username));
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        boolean match = bcryptMatches(rawPassword, user.getPassword());
        if (!match) {
            onFail(username);
            log.warn(String.format("login password mismatch, username=%s", username));
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        // 成功：清理失败计数
        String failKey = String.format(AUTH_FAIL_KEY, username);
        RedisUtil.delete(stringRedisTemplate, failKey);

        // 登录（使用 userId 作为 loginId），处理 rememberMe
        SaLoginModel model = new SaLoginModel();
        if (Boolean.TRUE.equals(rememberMe)) {
            // 记住我：持久化 Cookie
            model.setIsLastingCookie(true);
            // 7天
            model.setTimeout(7 * 24 * 60 * 60);
        }
        StpUtil.login(user.getId(), model);
        SaTokenInfo token = StpUtil.getTokenInfo();

        LoginResp resp = new LoginResp();
        // token 信息
        resp.setToken(token.getTokenValue());
        resp.setTokenType("Bearer");
        resp.setExpiresIn(Math.max(StpUtil.getTokenTimeout(), 0));

        // 用户信息（根据部门ID解析为名称；行业从缓存获取）
        LoginUserInfoResp ui = new LoginUserInfoResp();
        ui.setId(user.getId());
        ui.setUsername(user.getUsername());
        ui.setRealName(user.getRealName());
        ui.setEmail(user.getEmail());
        ui.setPhone(user.getPhone());
        if (user.getDepartmentId() != null) {
            SysDepartment dept = sysDepartmentRepo.findById(user.getDepartmentId());
            ui.setDepartment(dept != null ? dept.getDepartmentName() : "");
        } else {
            ui.setDepartment("");
        }
        // 角色装配建议接入权限服务后由其提供（此处不做硬编码）
        ui.setRole(null);
        ui.setStatus(user.getStatus() != null && user.getStatus() == 1 ? "active" : "disabled");
        ui.setIndustry(industryConfigCache.get().getType());
        ui.setAvatar(user.getAvatarUrl());
        resp.setUserInfo(ui);

        // 角色/权限/菜单（应用服务装配，后续可替换为专用权限子系统）
        String industryType = industryConfigCache.get().getType();
        resp.setRoles(permissionService.findRoleCodes(user.getId(), industryType));
        resp.setPermissions(permissionService.findPermissionCodes(user.getId(), industryType));
        resp.setMenus(permissionService.buildMenus(user.getId(), industryType));

        // 发布登录日志事件（异步）
        LoginLoggedEvent event = new LoginLoggedEvent();
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setStatus(1);
        event.setMessage("login success");
        eventPublisher.publishAsync(event);

        log.info(String.format("login success, userId=%s, username=%s", user.getId(), user.getUsername()));

        return resp;
    }

    /**
     * 校验明文密码与 bcrypt 哈希是否匹配
     *
     * @param raw  String 明文密码
     * @param hash String bcrypt 哈希
     * @return boolean 是否匹配
     */
    public static boolean bcryptMatches(String raw, String hash) {
        try {
            return BCrypt.checkpw(raw, hash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 登录失败计数与锁定处理
     *
     * @param username String 用户名
     */
    private void onFail(String username) {
        String failKey = String.format(AUTH_FAIL_KEY, username);
        Long cnt = 1L;
        String val = RedisUtil.get(stringRedisTemplate, failKey);
        if (StringUtils.hasText(val)) {
            try {
                cnt = Long.parseLong(val) + 1;
            } catch (Exception ignored) {
                cnt = 1L;
            }
        }
        RedisUtil.set(stringRedisTemplate, failKey, String.valueOf(cnt), java.time.Duration.ofSeconds(LOCK_SECONDS));
        if (cnt >= MAX_FAIL) {
            String lockKey = String.format(AUTH_LOCK_KEY, username);
            RedisUtil.set(stringRedisTemplate, lockKey, "1", java.time.Duration.ofSeconds(LOCK_SECONDS));
        }
        log.warn(String.format("login failed, username=%s, count=%s", username, cnt));
    }
}
