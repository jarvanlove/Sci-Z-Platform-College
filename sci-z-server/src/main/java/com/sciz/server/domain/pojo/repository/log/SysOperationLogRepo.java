package com.sciz.server.domain.pojo.repository.log;

import com.sciz.server.domain.pojo.entity.log.SysOperationLog;

/**
 * 操作日志仓储（领域层抽象）
 *
 * 将持久化细节屏蔽在基础设施层，由实现类调用 MyBatis-Plus Mapper 完成存取。
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
}
