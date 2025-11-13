package com.sciz.server.domain.pojo.repository.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import java.time.LocalDate;

/**
 * 登录日志仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className LoginLogRepo
 * @date 2025-10-30 11:00
 */
public interface LoginLogRepo {

    /**
     * 保存登录日志
     *
     * @param entity SysLoginLog 实体
     * @return 生成的主键ID
     */
    Long save(SysLoginLog entity);

    /**
     * 分页查询登录日志
     *
     * @param page      IPage<SysLoginLog> 分页对象
     * @param userId    Long 用户ID（必需，只查询该用户的日志）
     * @param status    Integer 登录状态（0失败 1成功，null表示全部）
     * @param startDate LocalDate 开始日期
     * @param endDate   LocalDate 结束日期
     * @param sortBy    String 排序字段
     * @param asc       boolean 是否升序
     * @return IPage<SysLoginLog> 分页结果
     */
    IPage<SysLoginLog> page(IPage<SysLoginLog> page, Long userId, Integer status, LocalDate startDate,
            LocalDate endDate,
            String sortBy, boolean asc);
}
