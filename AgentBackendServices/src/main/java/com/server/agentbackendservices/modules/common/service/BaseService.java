package com.server.agentbackendservices.modules.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.agentbackendservices.modules.common.entity.BaseEntity;
import com.server.agentbackendservices.modules.common.vo.ResultVO;

/**
 * 基础服务接口
 * 定义通用的业务方法
 *
 * @param <T> 实体类型
 * @author AgentBackendServices
 * @since 2024-01-01
 */
public interface BaseService<T extends BaseEntity> extends IService<T> {
    
    /**
     * 获取条件查询列表
     * @param entity 查询条件实体
     * @return 查询结果
     */
    ResultVO getWhereList(T entity);
}
