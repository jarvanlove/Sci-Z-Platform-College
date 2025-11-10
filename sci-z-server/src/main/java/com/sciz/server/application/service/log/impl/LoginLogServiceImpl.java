package com.sciz.server.application.service.log.impl;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.domain.pojo.repository.log.LoginLogRepo;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 登录日志应用服务实现
 *
 * @author JiaWen.Wu
 * @className LoginLogServiceImpl
 * @date 2025-11-07 17:50
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepo repository;

    public LoginLogServiceImpl(LoginLogRepo repository) {
        this.repository = repository;
    }

    @Override
    public Long saveFromEvent(LoginLoggedEvent event) {
        SysLoginLog entity = new SysLoginLog();
        entity.setUserId(event.getUserId());
        entity.setUsername(event.getUsername());
        entity.setLoginIp(event.getLoginIp());
        entity.setLoginLocation(event.getLoginLocation());
        entity.setBrowser(event.getBrowser());
        entity.setOs(event.getOs());
        entity.setStatus(event.getStatus());
        entity.setMessage(truncate(event.getMessage(), 255));
        entity.setLoginTime(event.getLoginTime());

        Long id = save(entity);
        log.info(String.format("[LoginLogService] saved loginLog: id=%s, username=%s, status=%s",
                id, entity.getUsername(), entity.getStatus()));
        return id;
    }

    @Override
    public Long save(SysLoginLog entity) {
        return repository.save(entity);
    }

    /**
     * 截取字符串
     *
     * @param s   字符串
     * @param max 最大长度
     * @return 截取后的字符串
     */
    private String truncate(String s, int max) {
        return Optional.ofNullable(s)
                .map(value -> value.length() <= max ? value : value.substring(0, max))
                .orElse(null);
    }
}
