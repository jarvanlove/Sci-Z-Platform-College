package com.sciz.server.infrastructure.shared.handler.user;

import com.sciz.server.infrastructure.external.mail.MailService;
import com.sciz.server.infrastructure.shared.event.user.UserLoginEvent;
import com.sciz.server.infrastructure.shared.event.user.UserLogoutEvent;
import com.sciz.server.infrastructure.shared.event.user.UserRegisteredEvent;
import com.sciz.server.infrastructure.shared.event.user.UserEmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户事件处理器
 * 处理用户相关的领域事件
 *
 * @author JiaWen.Wu
 * @className UserEventHandler
 * @date 2025-10-29 10:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventHandler {
    private final MailService mailService;

    /**
     * 处理用户注册事件
     *
     * @param event 用户注册事件
     */
    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("处理用户注册事件: userId={}, username={}",
                    event.getUserId(), event.getUsername());

            // 1. 发送欢迎邮件
            sendWelcomeEmail(event);

            // 2. 初始化用户设置
            initializeUserSettings(event);

            // 3. 记录审计日志
            logUserRegistration(event);

            // 4. 更新统计信息
            updateUserStatistics(event);

            log.info("用户注册事件处理完成: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("处理用户注册事件失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户登录事件
     *
     * @param event 用户登录事件
     */
    @EventListener
    @Async
    public void handleUserLogin(UserLoginEvent event) {
        try {
            log.info("处理用户登录事件: userId={}, loginTime={}, loginIp={}",
                    event.getUserId(), event.getLoginTime(), event.getLoginIp());

            // 1. 更新最后登录时间
            updateLastLoginTime(event);

            // 2. 记录登录日志
            logUserLogin(event);

            // 3. 检查异常登录
            checkAbnormalLogin(event);

            // 4. 更新在线状态
            updateOnlineStatus(event);

            log.info("用户登录事件处理完成: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("处理用户登录事件失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户退出事件
     *
     * @param event 用户退出事件
     */
    @EventListener
    @Async
    public void handleUserLogout(UserLogoutEvent event) {
        try {
            log.info("处理用户退出事件: userId={}, logoutTime={}",
                    event.getUserId(), event.getLogoutTime());

            // 1. 更新在线状态
            updateOfflineStatus(event);

            // 2. 记录退出日志
            logUserLogout(event);

            // 3. 清理用户会话
            cleanupUserSession(event);

            log.info("用户退出事件处理完成: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("处理用户退出事件失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户邮箱验证码事件
     *
     * @param event UserEmailVerificationEvent 事件
     */
    @EventListener
    @Async
    public void handleUserEmailVerification(UserEmailVerificationEvent event) {
        try {
            log.info(String.format("处理用户邮箱验证码事件: userId=%s, email=%s",
                    event.getUserId(), event.getEmail()));
            dispatchEmailVerification(event);
            log.info(String.format("用户邮箱验证码处理完成: userId=%s", event.getUserId()));
        } catch (Exception e) {
            log.error(String.format("处理用户邮箱验证码事件失败: userId=%s, email=%s, err=%s",
                    event.getUserId(), event.getEmail(), e.getMessage()), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 发送欢迎邮件
     */
    private void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info("发送欢迎邮件给用户: {}", event.getEmail());
        // 实现发送欢迎邮件的逻辑
    }

    /**
     * 初始化用户设置
     */
    private void initializeUserSettings(UserRegisteredEvent event) {
        log.info("初始化用户设置: userId={}", event.getUserId());
        // 实现初始化用户设置的逻辑
    }

    /**
     * 记录用户注册审计日志
     */
    private void logUserRegistration(UserRegisteredEvent event) {
        log.info("记录用户注册审计日志: userId={}, username={}, email={}",
                event.getUserId(), event.getUsername(), event.getEmail());
        // 实现记录审计日志的逻辑
    }

    /**
     * 更新用户统计信息
     */
    private void updateUserStatistics(UserRegisteredEvent event) {
        log.info("更新用户统计信息: userId={}", event.getUserId());
        // 实现更新统计信息的逻辑
    }

    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(UserLoginEvent event) {
        log.info("更新最后登录时间: userId={}", event.getUserId());
        // 实现更新最后登录时间的逻辑
    }

    /**
     * 记录用户登录日志
     */
    private void logUserLogin(UserLoginEvent event) {
        log.info("记录用户登录日志: userId={}, loginTime={}, loginIp={}",
                event.getUserId(), event.getLoginTime(), event.getLoginIp());
        // 实现记录登录日志的逻辑
    }

    /**
     * 检查异常登录
     */
    private void checkAbnormalLogin(UserLoginEvent event) {
        log.info("检查异常登录: userId={}, loginIp={}",
                event.getUserId(), event.getLoginIp());
        // 实现检查异常登录的逻辑
    }

    /**
     * 更新在线状态
     */
    private void updateOnlineStatus(UserLoginEvent event) {
        log.info("更新在线状态: userId={}", event.getUserId());
        // 实现更新在线状态的逻辑
    }

    /**
     * 更新离线状态
     */
    private void updateOfflineStatus(UserLogoutEvent event) {
        log.info("更新离线状态: userId={}", event.getUserId());
        // 实现更新离线状态的逻辑
    }

    /**
     * 记录用户退出日志
     */
    private void logUserLogout(UserLogoutEvent event) {
        log.info("记录用户退出日志: userId={}, logoutTime={}",
                event.getUserId(), event.getLogoutTime());
        // 实现记录退出日志的逻辑
    }

    /**
     * 清理用户会话
     */
    private void cleanupUserSession(UserLogoutEvent event) {
        log.info("清理用户会话: userId={}", event.getUserId());
        // 实现清理用户会话的逻辑
    }

    /**
     * 发送邮箱验证码
     *
     * @param event UserEmailVerificationEvent 事件
     */
    private void dispatchEmailVerification(UserEmailVerificationEvent event) {
        log.info(String.format("发送邮箱验证码: email=%s, provider=%s",
                event.getEmail(), event.getMailProviderType()));
        log.debug(String.format("验证码明文: email=%s, code=%s", event.getEmail(), event.getVerificationCode()));
        mailService.sendEmailVerificationCode(event.getMailProviderType(), event.getEmail(),
                event.getVerificationCode());
    }
}
