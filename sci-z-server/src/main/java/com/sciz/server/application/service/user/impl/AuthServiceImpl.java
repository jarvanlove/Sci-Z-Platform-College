package com.sciz.server.application.service.user.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 认证应用服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String AUTH_FAIL_KEY = "auth:fail:%s";
    private static final String AUTH_LOCK_KEY = "auth:lock:%s";
    private static final int MAX_FAIL = 6;
    private static final long LOCK_SECONDS = 30L * 60L;

    private final SysUserRepo sysUserRepo;
    private final StringRedisTemplate stringRedisTemplate;
    private final EventPublisher eventPublisher;

    public AuthServiceImpl(SysUserRepo sysUserRepo, StringRedisTemplate stringRedisTemplate,
            EventPublisher eventPublisher) {
        this.sysUserRepo = sysUserRepo;
        this.stringRedisTemplate = stringRedisTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LoginResp login(String username, String rawPassword, Boolean rememberMe) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        // 锁定校验
        String lockKey = String.format(AUTH_LOCK_KEY, username);
        String locked = RedisUtil.get(stringRedisTemplate, lockKey);
        if (StringUtils.hasText(locked)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), String.format("账号已锁定，请稍后再试"));
        }

        SysUser user = sysUserRepo.findByUsername(username);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            onFail(username);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), String.format("用户名或密码错误"));
        }

        boolean match = bcryptMatches(rawPassword, user.getPassword());
        if (!match) {
            onFail(username);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), String.format("用户名或密码错误"));
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
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setToken(token.getTokenValue());
        resp.setExpireTime(StpUtil.getTokenTimeout());

        // 发布登录日志事件（异步）
        LoginLoggedEvent event = new LoginLoggedEvent();
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setStatus(1);
        event.setMessage("login success");
        eventPublisher.publishAsync(event);

        return resp;
    }

    public static boolean bcryptMatches(String raw, String hash) {
        try {
            return BCrypt.checkpw(raw, hash);
        } catch (Exception e) {
            return false;
        }
    }

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
    }
}
