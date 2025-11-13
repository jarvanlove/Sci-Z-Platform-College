package com.sciz.server.application.service.log.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.domain.pojo.dto.request.log.LoginLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.LoginLogResp;
import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.domain.pojo.repository.log.LoginLogRepo;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
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

    @Override
    public PageResult<LoginLogResp> page(LoginLogQueryReq req) {
        // 获取当前登录用户ID
        var userId = LoginUserUtil.requireCurrentUserId();
        log.debug(String.format("查询登录日志: userId=%s", userId));

        var baseQuery = req.toBaseQuery();
        var page = new Page<SysLoginLog>(baseQuery.pageNo(), baseQuery.pageSize());

        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("loginTime");

        IPage<SysLoginLog> logPage = repository.page(page, userId, req.status(), req.startDate(), req.endDate(), sortBy,
                asc);

        var records = logPage.getRecords().stream()
                .map(log -> new LoginLogResp(
                        log.getId(),
                        log.getUserId(),
                        log.getUsername(),
                        log.getLoginIp(),
                        log.getLoginLocation(),
                        log.getBrowser(),
                        log.getOs(),
                        log.getStatus(),
                        log.getMessage(),
                        log.getLoginTime()))
                .toList();

        Page<LoginLogResp> resultPage = new Page<>(logPage.getCurrent(), logPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(logPage.getTotal());
        return PageResult.of(resultPage);
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
