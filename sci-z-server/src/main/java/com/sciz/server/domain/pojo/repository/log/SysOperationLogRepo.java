package com.sciz.server.domain.pojo.repository.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import java.time.LocalDateTime;

/**
 * 操作日志仓储（领域层抽象）
 *
 * @author JiaWen.Wu
 * @className SysOperationLogRepo
 * @date 2025-10-29 16:20
 */
public interface SysOperationLogRepo {

    /**
     * 保存操作日志
     *
     * @param entity SysOperationLog 实体
     * @return 生成的主键ID
     */
    Long save(SysOperationLog entity);

    /**
     * 分页查询操作日志
     *
     * @param page      IPage<SysOperationLog> 分页对象
     * @param username  String 用户名
     * @param operation String 操作名称
     * @param method    String 请求方法
     * @param status    Integer 状态
     * @param startTime LocalDateTime 开始时间
     * @param endTime   LocalDateTime 结束时间
     * @param sortBy    String 排序字段
     * @param asc       boolean 是否升序
     * @return IPage<SysOperationLog> 分页结果
     */
    IPage<SysOperationLog> page(IPage<SysOperationLog> page,
            String username,
            String operation,
            String method,
            Integer status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LogLevelStatus level,
            String sortBy,
            boolean asc);

    /**
     * 根据ID查询日志
     *
     * @param id Long 日志ID
     * @return SysOperationLog 日志
     */
    SysOperationLog findById(Long id);
}
