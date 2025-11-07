package com.sciz.server.application.service.user.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserInfoResp;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.infrastructure.shared.enums.UserStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.enums.LoginStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.CaptchaUtil;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.infrastructure.shared.utils.ClientInfoUtil;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

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
     * 用户登录（验证码校验 → 账号锁定校验 → 用户有效性校验 → 密码校验 → 清理失败计数 → Sa-Token 登录 → 组装返回 → 发布事件）
     *
     * @param loginReq LoginReq 登录请求（包含用户名、密码、验证码等）
     * @param request  HttpServletRequest 请求对象（用于获取客户端信息）
     * @return LoginResp 登录返回（包含 token、用户信息、角色/权限/菜单）
     */
    @Override
    public LoginResp login(LoginReq loginReq, HttpServletRequest request) {
        var username = loginReq.getUsername();
        var rawPassword = loginReq.getPassword();
        var rememberMe = loginReq.getRememberMe();
        var captcha = loginReq.getCaptcha();
        var captchaKey = loginReq.getCaptchaKey();

        // 1. 参数校验
        validateLoginParams(username, rawPassword);

        // 2. 验证码校验（登录失败次数 >= 5 次时需要验证码）
        validateCaptchaIfRequired(username, captcha, captchaKey);

        // 3. 账号锁定校验
        checkAccountLock(username);

        // 4. 用户有效性校验
        log.info(String.format("login start, username=%s", username));
        var user = sysUserRepo.findByUsername(username);
        validateUser(user, username);

        // 5. 密码校验
        validatePassword(rawPassword, user.getPassword(), username);

        // 6. 清理失败计数和验证码
        clearLoginFailCount(username);

        // 7. Sa-Token 登录
        var tokenInfo = performSaTokenLogin(user.getId(), rememberMe);

        // 8. 缓存 token
        cacheUserToken(user.getId(), tokenInfo);

        // 9. 组装返回结果
        var industryType = industryConfigCache.get().getType();
        var loginResp = buildLoginResponse(user, tokenInfo, industryType, rememberMe);

        // 10. 发布登录日志事件
        publishLoginLogEvent(user, request);

        log.info(String.format("login success, userId=%s, username=%s", user.getId(), user.getUsername()));
        return loginResp;
    }

    /**
     * 参数校验
     */
    private void validateLoginParams(String username, String rawPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            log.warn(String.format("login bad request, username=%s", username));
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }

    /**
     * 验证码校验（如果前端提供了验证码）
     * 前端在失败次数 >= 5 次时会主动获取验证码并提交
     */
    private void validateCaptchaIfRequired(String username, String captcha, String captchaKey) {
        // 如果前端没有提供验证码，跳过验证
        if (!StringUtils.hasText(captcha) && !StringUtils.hasText(captchaKey)) {
            return;
        }

        // 前端提供了验证码，但不完整
        if (!StringUtils.hasText(captcha) || !StringUtils.hasText(captchaKey)) {
            log.warn(String.format("验证码参数不完整, username=%s, hasCaptcha=%s, hasCaptchaKey=%s",
                    username, StringUtils.hasText(captcha), StringUtils.hasText(captchaKey)));
            throw new BusinessException(ResultCode.CAPTCHA_REQUIRED);
        }

        // 从 Redis 获取验证码
        var cacheKey = String.format(CacheConstant.CAPTCHA_KEY_PREFIX, captchaKey);
        var cachedCaptcha = RedisUtil.get(stringRedisTemplate, cacheKey);

        if (cachedCaptcha == null) {
            log.warn(String.format("验证码已过期, username=%s, captchaKey=%s", username, captchaKey));
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }

        // 验证码不匹配
        if (!CaptchaUtil.verify(captcha, cachedCaptcha)) {
            log.warn(String.format("验证码错误, username=%s, input=%s", username, captcha));
            throw new BusinessException(ResultCode.CAPTCHA_INVALID);
        }

        // 验证码校验成功，删除已使用的验证码
        RedisUtil.delete(stringRedisTemplate, cacheKey);
        log.debug(String.format("验证码校验成功: username=%s", username));
    }

    /**
     * 检查账号锁定状态
     */
    private void checkAccountLock(String username) {
        var lockKey = String.format(CacheConstant.AUTH_LOCK_KEY, username);
        var locked = RedisUtil.get(stringRedisTemplate, lockKey);
        if (StringUtils.hasText(locked)) {
            log.warn(String.format("login locked, username=%s", username));
            throw new BusinessException(ResultCode.USER_ACCOUNT_LOCKED);
        }
    }

    /**
     * 校验用户有效性
     */
    private void validateUser(SysUser user, String username) {
        if (user == null || (user.getStatus() != null && UserStatus.DISABLED.getCode().equals(user.getStatus()))) {
            onFail(username);
            log.warn(String.format("login user invalid, username=%s", username));
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.USER_LOGIN_FAILED.getMessage());
        }
    }

    /**
     * 校验密码
     */
    private void validatePassword(String rawPassword, String hashedPassword, String username) {
        var match = bcryptMatches(rawPassword, hashedPassword);
        if (!match) {
            onFail(username);
            log.warn(String.format("login password mismatch, username=%s", username));
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), ResultCode.USER_LOGIN_FAILED.getMessage());
        }
    }

    /**
     * 清理登录失败计数
     */
    private void clearLoginFailCount(String username) {
        var failKey = String.format(CacheConstant.AUTH_FAIL_KEY, username);
        RedisUtil.delete(stringRedisTemplate, failKey);
    }

    /**
     * 执行 Sa-Token 登录
     */
    private SaTokenInfo performSaTokenLogin(Long userId, Boolean rememberMe) {
        var model = new SaLoginModel();
        if (Boolean.TRUE.equals(rememberMe)) {
            model.setIsLastingCookie(true);
            model.setTimeout(7 * 24 * 60 * 60); // 7天
        }
        StpUtil.login(userId, model);
        return StpUtil.getTokenInfo();
    }

    /**
     * 缓存用户 token
     */
    private void cacheUserToken(Long userId, SaTokenInfo tokenInfo) {
        try {
            var tokenTtl = Math.max(StpUtil.getTokenTimeout(), 0);
            var tokenKey = String.format(CacheConstant.AUTH_TOKEN_KEY, userId);
            var tokenValue = tokenInfo.getTokenValue();

            if (tokenTtl > 0) {
                RedisUtil.set(stringRedisTemplate, tokenKey, tokenValue, Duration.ofSeconds(tokenTtl));
            } else {
                RedisUtil.set(stringRedisTemplate, tokenKey, tokenValue, null);
            }
        } catch (Exception e) {
            log.warn(String.format("save token to cache failed, userId=%s, err=%s", userId, e.getMessage()));
        }
    }

    /**
     * 组装登录响应
     */
    private LoginResp buildLoginResponse(SysUser user, SaTokenInfo tokenInfo, String industryType, Boolean rememberMe) {
        var resp = new LoginResp();

        // 设置 token 信息
        resp.setToken(tokenInfo.getTokenValue());
        resp.setTokenType("Bearer");
        resp.setExpiresIn(Math.max(StpUtil.getTokenTimeout(), 0));

        // 设置记住我标记
        resp.setRememberMe(Boolean.TRUE.equals(rememberMe));

        // 设置用户信息
        resp.setUserInfo(buildUserInfo(user, industryType));

        // 设置角色/权限/菜单
        resp.setRoles(permissionService.findRoleCodes(user.getId(), industryType));
        resp.setPermissions(permissionService.findPermissionCodes(user.getId(), industryType));
        resp.setMenus(permissionService.buildMenus(user.getId(), industryType));

        return resp;
    }

    /**
     * 组装用户信息
     */
    private LoginUserInfoResp buildUserInfo(SysUser user, String industryType) {
        var userInfo = new LoginUserInfoResp();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setDepartment(getDepartmentName(user.getDepartmentId()));
        userInfo.setRole(permissionService.getPrimaryRoleCode(user.getId(), industryType));
        userInfo.setStatus(getUserStatusDescription(user.getStatus()));
        userInfo.setIndustry(industryType);
        userInfo.setAvatar(user.getAvatarUrl());
        return userInfo;
    }

    /**
     * 获取部门名称
     */
    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return "";
        }
        var dept = sysDepartmentRepo.findById(departmentId);
        return dept != null ? dept.getDepartmentName() : "";
    }

    /**
     * 获取用户状态描述
     */
    private String getUserStatusDescription(Integer status) {
        if (EnableStatus.ENABLED.getCode().equals(status)) {
            return EnableStatus.ENABLED.getDescription();
        }
        return EnableStatus.DISABLED.getDescription();
    }

    /**
     * 发布登录日志事件
     */
    private void publishLoginLogEvent(SysUser user, HttpServletRequest request) {
        var clientIp = request != null ? ClientInfoUtil.getClientIp(request) : "unknown";
        var browser = request != null ? ClientInfoUtil.getBrowser(request) : "unknown";
        var os = request != null ? ClientInfoUtil.getOs(request) : "unknown";
        var location = ClientInfoUtil.getLocation(clientIp);
        var loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SystemConstant.DATE_TIME_FORMAT));

        var event = new LoginLoggedEvent();
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setLoginIp(clientIp);
        event.setLoginLocation(location);
        event.setBrowser(browser);
        event.setOs(os);
        event.setStatus(LoginStatus.SUCCESS.getCode());
        event.setMessage(ResultCode.USER_LOGIN_SUCCESS.getMessage());
        event.setLoginTime(loginTime);
        eventPublisher.publishAsync(event);
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
        String failKey = String.format(CacheConstant.AUTH_FAIL_KEY, username);
        Long cnt = 1L;
        String val = RedisUtil.get(stringRedisTemplate, failKey);
        if (StringUtils.hasText(val)) {
            try {
                cnt = Long.parseLong(val) + 1;
            } catch (Exception ignored) {
                cnt = 1L;
            }
        }
        RedisUtil.set(stringRedisTemplate, failKey, String.valueOf(cnt),
                java.time.Duration.ofSeconds(CacheConstant.AUTH_LOCK_DURATION));
        if (cnt >= CacheConstant.MAX_LOGIN_FAIL_COUNT) {
            String lockKey = String.format(CacheConstant.AUTH_LOCK_KEY, username);
            RedisUtil.set(stringRedisTemplate, lockKey, "1",
                    java.time.Duration.ofSeconds(CacheConstant.AUTH_LOCK_DURATION));
        }
        log.warn(String.format("login failed, username=%s, count=%s", username, cnt));
    }

    /**
     * 用户登出
     * 注销当前登录会话，清理 token 和权限缓存
     */
    @Override
    public void logout() {
        // 1. 获取当前登录用户信息
        var userId = StpUtil.getLoginIdAsLong();
        var industryType = industryConfigCache.get().getType();

        log.info(String.format("用户退出登录: userId=%s, industryType=%s", userId, industryType));

        // 2. 清理 token 缓存
        clearTokenCache(userId);

        // 3. 清理权限缓存（roles、permissions、menus）
        clearPermissionCache(userId, industryType);

        // 4. 调用 Sa-Token 登出
        StpUtil.logout();

        log.info(String.format("用户退出登录成功: userId=%s", userId));
    }

    /**
     * 清理用户 token 缓存
     *
     * @param userId Long 用户ID
     */
    private void clearTokenCache(Long userId) {
        var tokenKey = String.format(CacheConstant.AUTH_TOKEN_KEY, userId);
        RedisUtil.delete(stringRedisTemplate, tokenKey);
        log.debug(String.format("清理 token 缓存: userId=%s", userId));
    }

    /**
     * 清理用户权限缓存
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     */
    private void clearPermissionCache(Long userId, String industryType) {
        // 调用 PermissionService 清理权限缓存
        permissionService.refreshUserAuthCache(userId, industryType);
        log.debug(String.format("清理权限缓存: userId=%s, industryType=%s", userId, industryType));
    }

    /**
     * 获取验证码
     * 生成图形验证码并缓存到 Redis
     *
     * @return CaptchaResp 验证码响应
     */
    @Override
    public CaptchaResp getCaptcha() {
        // 1. 生成验证码文本（4位）
        var captchaText = CaptchaUtil.generateText();

        // 2. 生成验证码唯一标识（UUID）
        var captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 3. 缓存验证码文本到 Redis（5分钟过期）
        var cacheKey = String.format(CacheConstant.CAPTCHA_KEY_PREFIX, captchaKey);
        RedisUtil.set(stringRedisTemplate, cacheKey, captchaText, Duration.ofSeconds(CacheConstant.CAPTCHA_EXPIRE));

        // 4. 生成验证码图片（Base64）
        var captchaImage = CaptchaUtil.generateImage(captchaText);

        // 5. 组装响应（使用 Java 21 Record 构造方法）
        var resp = new CaptchaResp(captchaKey, captchaImage, CacheConstant.CAPTCHA_EXPIRE);

        log.debug(String.format("生成验证码成功: captchaKey=%s", captchaKey));
        return resp;
    }

}
