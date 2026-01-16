package com.sciz.server.application.service.user.strategy.impl;

import com.sciz.server.application.service.user.strategy.LoginStrategy;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.LoginTypeStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 密码登录策略
 *
 * @author JiaWen.Wu
 * @className PasswordLoginStrategy
 * @date 2025-01-15 14:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final SysUserRepo sysUserRepo;

    @Override
    public String getLoginType() {
        return LoginTypeStatus.PASSWORD.getCode();
    }

    @Override
    public void validateParams(LoginReq loginReq) {
        var account = loginReq.getUsername(); // 支持用户名、手机号、邮箱
        var password = loginReq.getPassword();

        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            log.warn(String.format("密码登录参数校验失败: account=%s", account));
            throw BusinessException.of(ResultCode.BAD_REQUEST, "账号和密码不能为空");
        }
    }

    @Override
    public SysUser findUser(LoginReq loginReq) {
        var account = loginReq.getUsername().trim(); // 支持用户名、手机号、邮箱
        log.info(String.format("密码登录查找用户: account=%s", account));
        
        // 尝试通过用户名查找
        var user = sysUserRepo.findByUsername(account);
        if (user != null) {
            return user;
        }
        
        // 尝试通过邮箱查找
        user = sysUserRepo.findByEmail(account.toLowerCase());
        if (user != null) {
            return user;
        }
        
        // 尝试通过手机号查找
        var normalizedPhone = normalizePhone(account);
        user = sysUserRepo.findByPhone(normalizedPhone);
        if (user != null) {
            return user;
        }
        
        // 所有方式都找不到用户
        log.warn(String.format("密码登录用户不存在: account=%s", account));
        throw BusinessException.of(ResultCode.USER_LOGIN_FAILED, "账号或密码错误");
    }
    
    /**
     * 规格化手机号（移除 +86 前缀，统一格式）
     *
     * @param phone String 原始手机号
     * @return String 规格化后的手机号
     */
    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        var trimmed = phone.trim();
        // 移除 +86 前缀
        if (trimmed.startsWith("+86")) {
            trimmed = trimmed.substring(3).trim();
        }
        // 移除开头的 0（如果有）
        if (trimmed.startsWith("0") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    @Override
    public void validateCredential(LoginReq loginReq, SysUser user) {
        var rawPassword = loginReq.getPassword();
        var hashedPassword = user.getPassword();
        var username = user.getUsername();

        var match = BCrypt.checkpw(rawPassword, hashedPassword);
        if (!match) {
            log.warn(String.format("密码登录密码校验失败: username=%s", username));
            throw BusinessException.of(ResultCode.USER_LOGIN_FAILED, "用户名或密码错误");
        }
    }

    @Override
    public String getFailCountKey(LoginReq loginReq) {
        // 使用账号作为失败计数标识（支持用户名、手机号、邮箱）
        return loginReq.getUsername();
    }
}

