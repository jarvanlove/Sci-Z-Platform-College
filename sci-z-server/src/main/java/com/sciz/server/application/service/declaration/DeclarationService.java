package com.sciz.server.application.service.declaration;

import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 申报应用服务
 * 
 * @author JiaWen.Wu
 * @className DeclarationService
 * @date 2025-01-20 15:00
 */
public interface DeclarationService {

    /**
     * 创建申报
     *
     * @param req 创建请求
     * @return 申报ID
     */
    Long create(DeclarationCreateReq req);

    /**
     * 分页查询申报列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<DeclarationListResp> page(DeclarationListQueryReq req);

    /**
     * 获取申报详情
     *
     * @param id 申报ID
     * @return 申报详情
     */
    DeclarationDetailResp findDetail(Long id);

    /**
     * 获取工作流状态
     *
     * @param id 申报ID
     * @return 工作流状态信息
     */
    DeclarationDetailResp.WorkflowResult getWorkflowStatus(Long id);
}
