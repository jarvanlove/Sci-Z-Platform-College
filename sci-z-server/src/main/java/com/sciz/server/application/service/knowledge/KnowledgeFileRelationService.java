package com.sciz.server.application.service.knowledge;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 知识库文件关联应用服务
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationService
 * @date 2025-01-28 16:00
 */
public interface KnowledgeFileRelationService {

    /**
     * 创建知识库文件关联
     *
     * @param req 创建请求
     * @return 响应
     */
    KnowledgeFileRelationResp create(KnowledgeFileRelationCreateReq req);

    /**
     * 更新知识库文件关联
     *
     * @param id 关联ID
     * @param req 更新请求
     * @return 响应
     */
    KnowledgeFileRelationResp update(String id, KnowledgeFileRelationUpdateReq req);

    /**
     * 删除知识库文件关联
     *
     * @param id 关联ID
     */
    void delete(String id);

    /**
     * 根据ID查询知识库文件关联详情
     *
     * @param id 关联ID
     * @return 响应
     */
    KnowledgeFileRelationResp findDetail(String id);

    /**
     * 分页查询知识库文件关联列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<KnowledgeFileRelationResp> page(KnowledgeFileRelationQueryReq req);
}

