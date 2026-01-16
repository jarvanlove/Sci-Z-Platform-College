package com.sciz.server.application.service.user.strategy.impl;

import com.sciz.server.application.service.user.strategy.LoginStrategy;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.enums.LoginTypeStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 短信登录策略
 *
 * @author JiaWen.Wu
 * @className SmsLoginStrategy
 * @date 2025-01-15 17:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsLoginStrategy implements LoginStrategy {

    private final SysUserRepo sysUserRepo;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String getLoginType() {
        return LoginTypeStatus.SMS.getCode();
    }

    @Override
    public void validateParams(LoginReq loginReq) {
        var phone = loginReq.getPhone();
        var smsCode = loginReq.getSmsCode();

        if (!StringUtils.hasText(phone)) {
            log.warn("短信登录参数校验失败: 手机号为空");
            throw BusinessException.of(ResultCode.BAD_REQUEST, "手机号不能为空");
        }

        if (!StringUtils.hasText(smsCode)) {
            log.warn(String.format("短信登录参数校验失败: phone=%s, 短信验证码为空", phone));
            throw BusinessException.of(ResultCode.BAD_REQUEST, "短信验证码不能为空");
        }
    }

    @Override
    public SysUser findUser(LoginReq loginReq) {
        var phone = normalizePhone(loginReq.getPhone());
        log.info(String.format("短信登录查找用户: phone=%s", phone));
        var user = sysUserRepo.findByPhone(phone);
        // 用户不存在时返回 null，由 AuthServiceImpl.login() 处理自动注册逻辑
        if (user == null) {
            log.info(String.format("短信登录用户不存在，将自动注册: phone=%s", phone));
        }
        return user;
    }

    @Override
    public void validateCredential(LoginReq loginReq, SysUser user) {
        var phone = normalizePhone(loginReq.getPhone());
        var smsCode = loginReq.getSmsCode();

        // 校验短信验证码
        validateSmsVerificationCode(phone, smsCode);

        // 验证码校验成功后，清理验证码缓存
        clearSmsVerificationCode(phone);
    }

    @Override
    public String getFailCountKey(LoginReq loginReq) {
        return normalizePhone(loginReq.getPhone());
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
     * 校验短信验证码
     *
     * @param phone   String 手机号
     * @param smsCode String 短信验证码
     */
    private void validateSmsVerificationCode(String phone, String smsCode) {
        if (!StringUtils.hasText(smsCode)) {
            log.warn(String.format("短信验证码为空: phone=%s", phone));
            throw BusinessException.of(ResultCode.SMS_CODE_INVALID);
        }

        var cacheKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_CODE_KEY, phone);
        var cachedCode = RedisUtil.get(stringRedisTemplate, cacheKey);
        if (!StringUtils.hasText(cachedCode)) {
            log.warn(String.format("短信验证码已过期: phone=%s", phone));
            throw BusinessException.of(ResultCode.SMS_CODE_EXPIRED);
        }

        if (!cachedCode.equals(smsCode.trim())) {
            log.warn(String.format("短信验证码错误: phone=%s, input=%s", phone, smsCode));
            throw BusinessException.of(ResultCode.SMS_CODE_INVALID);
        }
    }

    /**
     * 清理短信验证码缓存
     *
     * @param phone String 手机号
     */
    private void clearSmsVerificationCode(String phone) {
        var cacheKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_CODE_KEY, phone);
        var limitKey = String.format(CacheConstant.AUTH_SMS_VERIFICATION_RATE_LIMIT_KEY, phone);
        RedisUtil.delete(stringRedisTemplate, cacheKey);
        RedisUtil.delete(stringRedisTemplate, limitKey);
    }
}

