package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.application.service.user.LoginLogAndStatsService;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.LoginStatus;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        if (event.getStatus() != null && LoginStatus.SUCCESS.getCode().equals(event.getStatus())) {
            updateUserLoginStats(event);
        }
    }

    /**
     * 更新用户登录统计信息
     *
     * @param event 登录日志事件
     */
    private void updateUserLoginStats(LoginLoggedEvent event) {
        SysUser user = sysUserRepo.findById(event.getUserId());
        if (user == null) {
            log.warn(String.format("用户不存在，无法更新登录统计: userId=%s", event.getUserId()));
            return;
        }

        // 更新登录统计字段
        Integer currentCount = user.getLoginCount();
        if (currentCount == null) {
            currentCount = 0;
        }
        user.setLoginCount(currentCount + 1);

        LocalDateTime loginTime = event.getLoginTime() != null ? event.getLoginTime() : LocalDateTime.now();
        user.setLastLoginTime(loginTime);
        user.setLastLoginIp(event.getLoginIp());

        // 更新用户信息
        boolean updated = sysUserRepo.updateById(user);
        if (updated) {
            log.info(String.format("用户登录统计更新成功: userId=%s, loginCount=%s, lastLoginTime=%s, lastLoginIp=%s",
                    event.getUserId(), user.getLoginCount(), user.getLastLoginTime(), user.getLastLoginIp()));
        } else {
            log.warn(String.format("用户登录统计更新失败: userId=%s", event.getUserId()));
        }
    }

}
