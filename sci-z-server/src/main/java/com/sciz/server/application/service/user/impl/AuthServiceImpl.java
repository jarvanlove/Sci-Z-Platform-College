package com.sciz.server.application.service.user.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.domain.pojo.dto.request.user.EmailCodeSendReq;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.request.user.PhoneCodeSendReq;
import com.sciz.server.domain.pojo.dto.request.user.RegisterReq;
import com.sciz.server.domain.pojo.dto.request.user.ResetPasswordReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckLoginResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckRoleResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckPermResp;
import com.sciz.server.domain.pojo.dto.response.user.RefreshTokenResp;
import com.sciz.server.domain.pojo.dto.response.user.RegisterResp;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.external.sms.SmsService;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import com.sciz.server.infrastructure.shared.enums.LoginStatus;
import com.sciz.server.infrastructure.shared.enums.UserStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.user.UserEmailVerificationEvent;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.CaptchaUtil;
import com.sciz.server.infrastructure.shared.utils.ClientInfoUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.interfaces.converter.AuthConverter;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final SysRoleRepo sysRoleRepo;
    private final SysUserRoleRepo sysUserRoleRepo;
    private final AuthConverter authConverter;
    private final SmsService smsService;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public AuthServiceImpl(SysUserRepo sysUserRepo, StringRedisTemplate stringRedisTemplate,
            EventPublisher eventPublisher,
            com.sciz.server.application.service.user.PermissionService permissionService,
            IndustryConfigCache industryConfigCache,
            SysDepartmentRepo sysDepartmentRepo,
            SysRoleRepo sysRoleRepo,
            SysUserRoleRepo sysUserRoleRepo,
            AuthConverter authConverter,
            SmsService smsService) {
        this.sysUserRepo = sysUserRepo;
        this.stringRedisTemplate = stringRedisTemplate;
        this.eventPublisher = eventPublisher;
        this.permissionService = permissionService;
        this.industryConfigCache = industryConfigCache;
        this.sysDepartmentRepo = sysDepartmentRepo;
        this.sysRoleRepo = sysRoleRepo;
        this.sysUserRoleRepo = sysUserRoleRepo;
        this.authConverter = authConverter;
        this.smsService = smsService;
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

        // 2. 验证码校验（登录失败次数 >= 3 次时需要验证码）
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

        // 9. 写入登录用户上下文
        var industryType = industryConfigCache.get().getType();
        cacheLoginUserContext(user, industryType);

        // 10. 组装返回结果
        var loginResp = buildLoginResponse(user, tokenInfo, industryType, rememberMe);

        // 11. 发布登录日志事件
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
     * 前端在失败次数 >= 3 次时会主动获取验证码并提交
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
        var cachedCaptcha = Optional.ofNullable(RedisUtil.get(stringRedisTemplate, cacheKey))
                .orElseThrow(() -> {
                    log.warn(String.format("验证码已过期, username=%s, captchaKey=%s", username, captchaKey));
                    return new BusinessException(ResultCode.CAPTCHA_EXPIRED);
                });

        // 验证码不匹配
        var normalizedUserCaptcha = captcha.trim().toUpperCase(Locale.ROOT);
        var normalizedCachedCaptcha = cachedCaptcha.trim().toUpperCase(Locale.ROOT);

        if (!CaptchaUtil.verify(normalizedUserCaptcha, normalizedCachedCaptcha)) {
            log.warn(String.format("验证码错误, username=%s, input=%s", username, captcha));
            throw new BusinessException(ResultCode.CAPTCHA_INVALID);
        }

        // 验证码校验成功，删除已使用的验证码
        RedisUtil.delete(stringRedisTemplate, cacheKey);
        log.debug(String.format("验证码校验成功: username=%s", username));
    }

    /**
     * 强制校验图形验证码（注册、重置密码等场景）
     *
     * @param captcha    String 验证码文本
     * @param captchaKey String 验证码唯一标识
     */
    private void validateCaptchaStrict(String captcha, String captchaKey) {
        if (!StringUtils.hasText(captcha) || !StringUtils.hasText(captchaKey)) {
            log.warn("captcha missing for strict validation");
            throw new BusinessException(ResultCode.CAPTCHA_REQUIRED);
        }

        var cacheKey = String.format(CacheConstant.CAPTCHA_KEY_PREFIX, captchaKey);
        var cachedCaptcha = RedisUtil.get(stringRedisTemplate, cacheKey);
        if (!StringUtils.hasText(cachedCaptcha)) {
            log.warn(String.format("captcha expired or not found, captchaKey=%s", captchaKey));
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }

        var normalizedUserCaptcha = captcha.trim().toUpperCase(Locale.ROOT);
        var normalizedCachedCaptcha = cachedCaptcha.trim().toUpperCase(Locale.ROOT);
        if (!CaptchaUtil.verify(normalizedUserCaptcha, normalizedCachedCaptcha)) {
            log.warn(String.format("captcha invalid, input=%s", captcha));
            throw new BusinessException(ResultCode.CAPTCHA_INVALID);
        }

        RedisUtil.delete(stringRedisTemplate, cacheKey);
        log.debug(String.format("captcha strict validation success, captchaKey=%s", captchaKey));
    }

    /**
     * 校验注册验证码
     */
    /**
     * 校验账号唯一性
     */
    private void ensureAccountUniqueness(String username, String email, String phone) {
        Optional.ofNullable(sysUserRepo.findByUsername(username))
                .ifPresent(existing -> {
                    log.warn(String.format("register username exists, username=%s", username));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
                });
        Optional.ofNullable(sysUserRepo.findByEmail(email))
                .ifPresent(existing -> {
                    log.warn(String.format("register email exists, email=%s", email));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "邮箱已被注册");
                });
        Optional.ofNullable(sysUserRepo.findByPhone(phone))
                .ifPresent(existing -> {
                    log.warn(String.format("register phone exists, phone=%s", phone));
                    throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "手机号已被注册");
                });
    }

    /**
     * 构建用户实体
     *
     * @param username     String 用户名
     * @param realName     String 真实姓名
     * @param email        String 邮箱
     * @param phone        String 手机号
     * @param rawPassword  String 明文密码
     * @param industryType String 行业类型
     * @param departmentId Long 部门ID
     * @return SysUser 用户实体
     */
    private SysUser buildUserEntity(String username, String realName, String email, String phone,
            String rawPassword, String industryType, Long departmentId) {
        var user = new SysUser();
        var now = LocalDateTime.now();
        user.setUsername(username);
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(hashPassword(rawPassword));
        user.setIndustryType(industryType);
        user.setDepartmentId(departmentId);
        user.setEmployeeId(generateEmployeeId(industryType, username));
        user.setStatus(EnableStatus.ENABLED.getCode());
        user.setLoginCount(0);
        user.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        return user;
    }

    /**
     * 生成行业内唯一的员工ID
     *
     * @param industryType String 行业类型
     * @param username     String 用户名
     * @return String 员工ID
     */
    private String generateEmployeeId(String industryType, String username) {
        var normalizedIndustry = Optional.ofNullable(industryType)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .orElse("GLOBAL");
        var normalizedUsername = username.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
        if (normalizedUsername.length() > 16) {
            normalizedUsername = normalizedUsername.substring(0, 16);
        }
        return String.format("%s-%s-%s", normalizedIndustry, normalizedUsername,
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
    }

    /**
     * 为新注册用户绑定默认角色
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     */
    private void assignDefaultRole(Long userId, String industryType) {
        Optional.ofNullable(sysRoleRepo.findByCode(SystemConstant.DEFAULT_USER_ROLE_CODE, industryType))
                .ifPresentOrElse(defaultRole -> {
                    var now = LocalDateTime.now();
                    var userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(defaultRole.getId());
                    userRole.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
                    userRole.setCreatedTime(now);
                    userRole.setUpdatedTime(now);
                    Optional.ofNullable(sysUserRoleRepo.save(userRole))
                            .ifPresentOrElse(id -> permissionService.refreshUserAuthCache(userId, industryType),
                                    () -> log.error(String.format("绑定默认角色失败: userId=%s, roleId=%s", userId,
                                            defaultRole.getId())));
                }, () -> log.warn(String.format("默认角色不存在: code=%s, industryType=%s",
                        SystemConstant.DEFAULT_USER_ROLE_CODE, industryType)));
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
        Optional.ofNullable(user)
                .filter(current -> !Optional.ofNullable(current.getStatus())
                        .map(UserStatus.DISABLED.getCode()::equals)
                        .orElse(false))
                .orElseThrow(() -> {
                    onFail(username);
                    log.warn(String.format("login user invalid, username=%s", username));
                    return new BusinessException(ResultCode.UNAUTHORIZED.getCode(),
                            ResultCode.USER_LOGIN_FAILED.getMessage());
                });
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

        // 设置用户信息（基础字段由转换器生成，其余业务字段由当前方法补齐）
        var userInfo = authConverter.toLoginUserInfoResp(user);
        userInfo.setDepartment(getDepartmentName(user.getDepartmentId()));
        userInfo.setRole(permissionService.getPrimaryRoleCode(user.getId(), industryType));
        userInfo.setStatus(getUserStatusDescription(user.getStatus()));
        userInfo.setIndustry(industryType);
        userInfo.setAvatar(user.getAvatarUrl());
        resp.setUserInfo(userInfo);

        // 设置角色/权限/菜单
        resp.setRoles(permissionService.findRoleCodes(user.getId(), industryType));
        resp.setPermissions(permissionService.findPermissionCodes(user.getId(), industryType));
        resp.setMenus(permissionService.buildMenus(user.getId(), industryType));

        return resp;
    }

    /**
     * 获取部门名称
     */
    private String getDepartmentName(Long departmentId) {
        return Optional.ofNullable(departmentId)
                .flatMap(id -> Optional.ofNullable(sysDepartmentRepo.findById(id)))
                .map(SysDepartment::getDepartmentName)
                .orElse("");
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
        var clientIp = Optional.ofNullable(request)
                .map(ClientInfoUtil::getClientIp)
                .orElse("unknown");
        var browser = Optional.ofNullable(request)
                .map(ClientInfoUtil::getBrowser)
                .orElse("unknown");
        var os = Optional.ofNullable(request)
                .map(ClientInfoUtil::getOs)
                .orElse("unknown");
        var location = ClientInfoUtil.getLocation(clientIp);
        var loginTime = LocalDateTime.now();

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
     * 密码哈希
     *
     * @param rawPassword String 明文密码
     * @return String bcrypt 哈希
     */
    private String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
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
     * 生成六位验证码
     *
     * @return String 数字验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    /**
     * 校验邮箱验证码发送频率
     *
     * @param email String 邮箱
     */
    private void ensureEmailVerificationRateLimit(String email) {
        var limitKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_RATE_LIMIT_KEY, email);
        var exists = RedisUtil.get(stringRedisTemplate, limitKey);
        if (StringUtils.hasText(exists)) {
            log.warn(String.format("邮箱验证码发送频繁: email=%s", email));
            throw new BusinessException(ResultCode.EMAIL_CODE_RATE_LIMITED);
        }
    }

    /**
     * 缓存邮箱验证码
     *
     * @param email     String 邮箱
     * @param emailCode String 验证码
     */
    private void cacheEmailVerificationCode(String email, String emailCode) {
        var cacheKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_CODE_KEY, email);
        RedisUtil.set(stringRedisTemplate, cacheKey, emailCode,
                Duration.ofSeconds(CacheConstant.AUTH_EMAIL_VERIFICATION_CODE_EXPIRE));

        var limitKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_RATE_LIMIT_KEY, email);
        RedisUtil.set(stringRedisTemplate, limitKey, "1",
                Duration.ofSeconds(CacheConstant.AUTH_EMAIL_VERIFICATION_CODE_INTERVAL));
    }

    /**
     * 校验邮箱验证码
     *
     * @param email     String 邮箱
     * @param emailCode String 验证码
     */
    private void validateEmailVerificationCode(String email, String emailCode) {
        if (!StringUtils.hasText(emailCode)) {
            throw new BusinessException(ResultCode.EMAIL_CODE_INVALID);
        }

        var cacheKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_CODE_KEY, email);
        var cachedCode = RedisUtil.get(stringRedisTemplate, cacheKey);
        if (!StringUtils.hasText(cachedCode)) {
            throw new BusinessException(ResultCode.EMAIL_CODE_EXPIRED);
        }

        if (!cachedCode.equals(emailCode.trim())) {
            throw new BusinessException(ResultCode.EMAIL_CODE_INVALID);
        }
    }

    /**
     * 清理邮箱验证码缓存
     *
     * @param email String 邮箱
     */
    private void clearEmailVerificationCode(String email) {
        var cacheKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_CODE_KEY, email);
        var limitKey = String.format(CacheConstant.AUTH_EMAIL_VERIFICATION_RATE_LIMIT_KEY, email);
        RedisUtil.delete(stringRedisTemplate, cacheKey);
        RedisUtil.delete(stringRedisTemplate, limitKey);
    }

    /**
     * 校验短信验证码发送频率
     *
     * @param phone String 手机号
     */
    private void ensureSmsVerificationRateLimit(String phone) {
        var limitKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_RATE_LIMIT_KEY, phone);
        var exists = RedisUtil.get(stringRedisTemplate, limitKey);
        if (StringUtils.hasText(exists)) {
            log.warn(String.format("短信验证码发送频繁: phone=%s", phone));
            throw new BusinessException(ResultCode.SMS_CODE_RATE_LIMITED);
        }
    }

    /**
     * 缓存短信验证码
     *
     * @param phone   String 手机号
     * @param smsCode String 验证码
     */
    private void cacheSmsVerificationCode(String phone, String smsCode) {
        var cacheKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_CODE_KEY, phone);
        RedisUtil.set(stringRedisTemplate, cacheKey, smsCode,
                Duration.ofSeconds(CacheConstant.AUTH_SMS_VERIFICATION_CODE_EXPIRE));

        var limitKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_RATE_LIMIT_KEY, phone);
        RedisUtil.set(stringRedisTemplate, limitKey, "1",
                Duration.ofSeconds(CacheConstant.AUTH_SMS_VERIFICATION_CODE_INTERVAL));
    }

    /**
     * 发送短信验证码：规格化手机号 → 校验图形验证码 → 校验账号存在 → 校验发送频率 → 生成并缓存验证码 → 调用短信服务
     *
     * @param req PhoneCodeSendReq 请求参数
     */
    @Override
    public void sendSmsVerificationCode(PhoneCodeSendReq req) {
        var phone = Optional.ofNullable(req.phone())
                .map(this::normalizePhone)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "手机号不能为空"));

        log.info(String.format("发送短信验证码: phone=%s", phone));

        validateCaptchaStrict(req.captcha(), req.captchaKey());

        var user = Optional.ofNullable(sysUserRepo.findByPhone(phone))
                .orElseThrow(() -> {
                    log.warn(String.format("发送短信验证码失败，手机号不存在: phone=%s", phone));
                    return new BusinessException(ResultCode.PHONE_NOT_FOUND);
                });

        ensureSmsVerificationRateLimit(phone);

        var verificationCode = generateVerificationCode();
        cacheSmsVerificationCode(phone, verificationCode);
        smsService.sendVerificationCode(phone, verificationCode);

        log.info(String.format("短信验证码发送成功: userId=%s, phone=%s", user.getId(), phone));
    }

    /**
     * 规格化邮箱
     *
     * @param email String 邮箱
     * @return String 规格化后的邮箱
     */
    private String normalizeEmail(String email) {
        return Optional.ofNullable(email)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .orElse(null);
    }

    /**
     * 规格化手机号
     *
     * @param phone String 手机号
     * @return String 规格化后的手机号
     */
    private String normalizePhone(String phone) {
        return Optional.ofNullable(phone)
                .map(String::trim)
                .orElse(null);
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

        // 4. 清理登录用户上下文并调用 Sa-Token 登出
        LoginUserUtil.clearCurrentUser();
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

    /**
     * 发送邮箱验证码：规格化邮箱 → 校验图形验证码 → 校验账号存在 → 校验发送频率 → 生成并缓存验证码 → 发布通知事件
     *
     * @param req EmailCodeSendReq 请求参数（包含邮箱、图形验证码、图形验证码标识）
     */
    @Override
    public void sendEmailVerificationCode(EmailCodeSendReq req) {
        var email = Optional.ofNullable(req.email())
                .map(this::normalizeEmail)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "邮箱不能为空"));

        log.info(String.format("发送邮箱验证码: email=%s", email));

        validateCaptchaStrict(req.captcha(), req.captchaKey());

        var user = Optional.ofNullable(sysUserRepo.findByEmail(email))
                .orElseThrow(() -> {
                    log.warn(String.format("发送邮箱验证码失败，邮箱不存在: email=%s", email));
                    return new BusinessException(ResultCode.EMAIL_NOT_FOUND);
                });

        ensureEmailVerificationRateLimit(email);

        var verificationCode = generateVerificationCode();
        cacheEmailVerificationCode(email, verificationCode);

        var mailProvider = Optional.ofNullable(req.provider())
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "邮箱服务商不能为空"));

        log.info(String.format("邮箱验证码生成成功: email=%s, provider=%s, code=%s",
                email, mailProvider, verificationCode));
        var event = new UserEmailVerificationEvent(user.getId(), email, verificationCode, mailProvider);
        eventPublisher.publishAsync(event);
    }

    /**
     * 重置密码：规格化邮箱 → 校验图形验证码 → 校验邮箱验证码 → 更新密码并清理验证码缓存
     *
     * @param req ResetPasswordReq 重置密码请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordReq req) {
        var email = Optional.ofNullable(req.email())
                .map(this::normalizeEmail)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(ResultCode.BAD_REQUEST, "邮箱不能为空"));
        log.info(String.format("重置密码开始: email=%s", email));

        validateCaptchaStrict(req.captcha(), req.captchaKey());

        var user = Optional.ofNullable(sysUserRepo.findByEmail(email))
                .orElseThrow(() -> {
                    log.warn(String.format("重置密码失败，邮箱不存在: email=%s", email));
                    return new BusinessException(ResultCode.EMAIL_NOT_FOUND);
                });

        validateEmailVerificationCode(email, req.emailCode());

        user.setPassword(hashPassword(req.newPassword()));
        user.setUpdatedTime(LocalDateTime.now());

        if (!sysUserRepo.updateById(user)) {
            log.error(String.format("重置密码更新数据库失败: userId=%s, email=%s", user.getId(), email));
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        clearEmailVerificationCode(email);
        log.info(String.format("重置密码成功: userId=%s, email=%s", user.getId(), email));
    }

    /**
     * 用户注册:规格化入参 → 校验验证码 → 校验账号唯一性 → 校验院系有效性 → 构造实体并入库 → 返回注册结果
     *
     * @param registerReq RegisterReq 注册请求
     * @return RegisterResp 注册结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResp register(RegisterReq registerReq) {
        // 1. 规格化并提取入参
        var username = registerReq.getUsername().trim();
        var realName = registerReq.getRealName().trim();
        var email = registerReq.getEmail().trim().toLowerCase(Locale.ROOT);
        var phone = registerReq.getPhone().trim();
        var departmentCode = registerReq.getDepartment().trim().toUpperCase(Locale.ROOT);

        log.info(String.format("register start, username=%s, email=%s, phone=%s, department=%s",
                username, email, phone, departmentCode));

        // 2. 校验验证码 & 账号唯一性
        validateCaptchaStrict(registerReq.getCaptcha(), registerReq.getCaptchaKey());
        ensureAccountUniqueness(username, email, phone);

        // 3. 校验院系/行业信息
        var industryView = industryConfigCache.get();
        var industryType = industryView.getType();
        var department = Optional.ofNullable(sysDepartmentRepo.findByCode(industryType, departmentCode))
                .orElseThrow(() -> {
                    log.warn(String.format("register department invalid, departmentCode=%s, industryType=%s",
                            departmentCode, industryType));
                    return new BusinessException(ResultCode.BAD_REQUEST, "院系/部门不存在或已停用");
                });

        // 4. 构造并持久化用户实体
        var user = buildUserEntity(username, realName, email, phone, registerReq.getPassword(),
                industryType, department.getId());

        Optional.ofNullable(sysUserRepo.save(user))
                .orElseThrow(() -> {
                    log.error(String.format("register failed when saving user, username=%s", username));
                    return new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
                });

        // 5. 绑定默认角色并刷新权限缓存
        assignDefaultRole(user.getId(), industryType);

        // 6. 返回注册结果
        log.info(String.format("register success, userId=%s, username=%s", user.getId(), username));
        return authConverter.toRegisterResp(user);
    }

    /**
     * 查询当前登录用户档案：校验登录态 → 加载用户与行业信息 → 汇总角色、权限、菜单及 token 信息
     *
     * @return ProfileResp 用户档案
     */
    @Override
    public ProfileResp profile() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        var userId = StpUtil.getLoginIdAsLong();
        var user = Optional.ofNullable(sysUserRepo.findById(userId))
                .filter(found -> !DeleteStatus.DELETED.getCode().equals(found.getIsDeleted()))
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        if (UserStatus.DISABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }

        var industryType = Optional.ofNullable(user.getIndustryType())
                .filter(StringUtils::hasText)
                .orElseGet(() -> industryConfigCache.get().getType());

        var userInfo = authConverter.toLoginUserInfoResp(user);
        userInfo.setDepartment(getDepartmentName(user.getDepartmentId()));
        userInfo.setRole(permissionService.getPrimaryRoleCode(user.getId(), industryType));
        userInfo.setStatus(getUserStatusDescription(user.getStatus()));
        userInfo.setIndustry(industryType);
        userInfo.setAvatar(user.getAvatarUrl());

        var roles = permissionService.findRoleCodes(user.getId(), industryType);
        var permissions = permissionService.findPermissionCodes(user.getId(), industryType);
        var menus = permissionService.buildMenus(user.getId(), industryType);

        var tokenInfo = StpUtil.getTokenInfo();
        var tokenTimeout = Math.max(StpUtil.getTokenTimeout(), 0);

        return authConverter.toProfileResp(
                user,
                tokenInfo,
                userInfo,
                roles,
                permissions,
                menus,
                tokenTimeout);
    }

    /**
     * 刷新或续期当前会话 Token：校验登录态 → 按需续期 token → 返回最新的 token 详情
     *
     * @return RefreshTokenResp 刷新结果
     */
    @Override
    public RefreshTokenResp refreshToken() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 续期逻辑：若当前 token 有限期，则续至配置的会话时长与当前剩余的较大值
        var currentTimeout = StpUtil.getTokenTimeout();
        if (currentTimeout > 0) {
            var targetTimeout = Math.max(currentTimeout, SystemConstant.USER_SESSION_EXPIRE);
            StpUtil.renewTimeout(targetTimeout);
        }

        var loginId = String.valueOf(StpUtil.getLoginId());
        var tokenInfo = StpUtil.getTokenInfo();
        var expiresIn = Math.max(StpUtil.getTokenTimeout(), 0);
        var expiresAt = expiresIn > 0 ? LocalDateTime.now(DEFAULT_ZONE).plusSeconds(expiresIn) : null;

        log.info(String.format("refresh token success: loginId=%s, expiresIn=%s", loginId, expiresIn));
        return authConverter.toRefreshTokenResp(loginId, tokenInfo, expiresIn, expiresAt);
    }

    /**
     * 校验当前是否登录：返回是否登录、登录 ID、token 剩余时间及 token 信息
     *
     * @return CheckLoginResp 登录校验结果
     */
    @Override
    public CheckLoginResp checkLogin() {
        if (!StpUtil.isLogin()) {
            return new CheckLoginResp(false, null, 0L, null, null);
        }
        var loginId = String.valueOf(StpUtil.getLoginId());
        var tokenInfo = StpUtil.getTokenInfo();
        var expiresIn = Math.max(StpUtil.getTokenTimeout(), 0);
        return new CheckLoginResp(true, loginId, expiresIn, tokenInfo.getTokenName(), tokenInfo.getTokenValue());
    }

    /**
     * 校验当前登录用户是否拥有指定角色：规格化角色编码 → 根据行业类型查询角色列表并判断是否命中
     *
     * @param roleCode     String 角色编码
     * @param industryType String 行业类型（可为空，为空时使用当前行业配置）
     * @return CheckRoleResp 角色校验结果
     */
    @Override
    public CheckRoleResp checkRole(String roleCode, String industryType) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "roleCode 不能为空");
        }

        var normalizedRoleCode = roleCode.trim();
        var normalizedIndustry = resolveIndustryType(industryType);
        var hasRole = hasLoginAnd(() -> {
            var userId = StpUtil.getLoginIdAsLong();
            List<String> roleCodes = permissionService.findRoleCodes(userId, normalizedIndustry);
            return roleCodes.stream().anyMatch(normalizedRoleCode::equals);
        });

        return new CheckRoleResp(normalizedRoleCode, normalizedIndustry, hasRole);
    }

    /**
     * 校验当前登录用户是否拥有指定权限：规格化权限编码 → 根据行业类型查询权限列表并判断是否命中
     *
     * @param permissionCode String 权限编码
     * @param industryType   String 行业类型（可为空，为空时使用当前行业配置）
     * @return CheckPermResp 权限校验结果
     */
    @Override
    public CheckPermResp checkPermission(String permissionCode, String industryType) {
        if (!StringUtils.hasText(permissionCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "permissionCode 不能为空");
        }
        var normalizedPermissionCode = permissionCode.trim();
        var normalizedIndustry = resolveIndustryType(industryType);
        var hasPermission = hasLoginAnd(() -> {
            var userId = StpUtil.getLoginIdAsLong();
            List<String> permissionCodes = permissionService.findPermissionCodes(userId, normalizedIndustry);
            return permissionCodes.stream().anyMatch(normalizedPermissionCode::equals);
        });
        return new CheckPermResp(normalizedPermissionCode, normalizedIndustry, hasPermission);
    }

    /**
     * 校验当前是否登录并执行指定操作
     *
     * @param supplier java.util.function.Supplier<Boolean> 操作提供者
     * @return boolean 是否执行操作
     */
    private boolean hasLoginAnd(Supplier<Boolean> supplier) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        return Boolean.TRUE.equals(supplier.get());
    }

    /**
     * 解析行业类型
     *
     * @param industryType String 行业类型
     * @return String 解析后的行业类型
     */
    private String resolveIndustryType(String industryType) {
        return StringUtils.hasText(industryType) ? industryType : industryConfigCache.get().getType();
    }

    /**
     * 缓存登录用户上下文
     *
     * @param user         SysUser 用户实体
     * @param industryType String 行业类型
     */
    private void cacheLoginUserContext(SysUser user, String industryType) {
        if (user == null) {
            return;
        }
        var departmentName = getDepartmentName(user.getDepartmentId());
        var context = LoginUserContext.of(user.getId(),
                user.getUsername(),
                user.getRealName(),
                industryType,
                user.getDepartmentId(),
                departmentName,
                user.getEmail(),
                user.getPhone());
        LoginUserUtil.cacheCurrentUser(context);
    }
}
