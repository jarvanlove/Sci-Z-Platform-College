package com.sciz.server.domain.pojo.repository.log.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.domain.pojo.mapper.log.SysLoginLogMapper;
import com.sciz.server.domain.pojo.repository.log.LoginLogRepo;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 登录日志仓储实现
 * 
 * @author JiaWen.Wu
 * @className LoginLogRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class LoginLogRepoImpl implements LoginLogRepo {

    private final SysLoginLogMapper mapper;

    public LoginLogRepoImpl(SysLoginLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysLoginLog entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public IPage<SysLoginLog> page(IPage<SysLoginLog> page, Long userId, Integer status, LocalDate startDate,
            LocalDate endDate,
            String sortBy, boolean asc) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();

        // 用户ID筛选（必需，只查询当前用户的日志）
        wrapper.eq(SysLoginLog::getUserId, userId);

        // 状态筛选
        Optional.ofNullable(status)
                .ifPresent(value -> wrapper.eq(SysLoginLog::getStatus, value));

        // 日期范围筛选
        if (startDate != null) {
            wrapper.ge(SysLoginLog::getLoginTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(SysLoginLog::getLoginTime, endDate.atTime(23, 59, 59));
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "loginTime" -> wrapper.orderBy(true, asc, SysLoginLog::getLoginTime);
                case "status" -> wrapper.orderBy(true, asc, SysLoginLog::getStatus);
                default -> wrapper.orderBy(true, false, SysLoginLog::getLoginTime);
            }
        } else {
            wrapper.orderBy(true, false, SysLoginLog::getLoginTime);
        }

        return mapper.selectPage(page, wrapper);
    }
}
