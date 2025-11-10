package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.application.service.user.LoginLogAndStatsService;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.LoginStatus;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志和统计服务实现类
 * 在同一事务中保存登录日志并更新用户统计信息
 *
 * @author JiaWen.Wu
 * @className LoginLogAndStatsServiceImpl
 * @date 2025-11-07 18:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogAndStatsServiceImpl implements LoginLogAndStatsService {

    private final LoginLogService loginLogService;
    private final SysUserRepo sysUserRepo;

    /**
     * 保存登录日志并更新用户统计（在同一事务中）
     *
     * @param event 登录日志事件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLoginLogAndUpdateStats(LoginLoggedEvent event) {
        // 1. 保存登录日志到 sys_login_log
        Long logId = loginLogService.saveFromEvent(event);
        log.debug(String.format("保存登录日志成功: logId=%s, userId=%s", logId, event.getUserId()));

        // 2. 更新用户登录统计（仅成功登录时更新）
        Optional.ofNullable(event.getStatus())
                .filter(LoginStatus.SUCCESS.getCode()::equals)
                .ifPresent(status -> updateUserLoginStats(event));
    }

    /**
     * 更新用户登录统计信息
     *
     * @param event 登录日志事件
     */
    private void updateUserLoginStats(LoginLoggedEvent event) {
        Optional.ofNullable(sysUserRepo.findById(event.getUserId()))
                .ifPresentOrElse(user -> {
                    var currentCount = Optional.ofNullable(user.getLoginCount()).orElse(0);
                    user.setLoginCount(currentCount + 1);

                    var loginTime = Optional.ofNullable(event.getLoginTime()).orElseGet(LocalDateTime::now);
                    user.setLastLoginTime(loginTime);
                    user.setLastLoginIp(event.getLoginIp());

                    if (sysUserRepo.updateById(user)) {
                        log.info(String.format("用户登录统计更新成功: userId=%s, loginCount=%s, lastLoginTime=%s, lastLoginIp=%s",
                                event.getUserId(), user.getLoginCount(), user.getLastLoginTime(),
                                user.getLastLoginIp()));
                    } else {
                        log.warn(String.format("用户登录统计更新失败: userId=%s", event.getUserId()));
                    }
                }, () -> log.warn(String.format("用户不存在，无法更新登录统计: userId=%s", event.getUserId())));
    }

}
