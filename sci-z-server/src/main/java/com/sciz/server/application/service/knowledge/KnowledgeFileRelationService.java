package com.sciz.server.application.service.knowledge;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationQueryReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderWithFilesResp;
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

    /**
     * 根据知识库ID分页查询文件夹及文件列表（树形结构）
     * 只返回有文件的文件夹，没有绑定文件夹的文档会显示在"未分类"文件夹中
     *
     * @param knowledgeId 知识库ID
     * @param folderId 文件夹ID（可选，null或0表示根目录）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 文件夹及文件列表（分页，混合分页：文件夹在前，文件在后）
     */
    PageResult<KnowledgeFolderWithFilesResp> listFoldersWithFiles(Long knowledgeId, Long folderId, Integer page, Integer size);

}

