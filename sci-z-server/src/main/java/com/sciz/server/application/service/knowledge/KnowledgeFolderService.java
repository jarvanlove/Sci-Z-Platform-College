package com.sciz.server.application.service.knowledge;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderResp;

import java.util.List;

/**
 * 知识库文件夹应用服务
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderService
 * @date 2025-01-28 16:00
 */
public interface KnowledgeFolderService {
    /**
     * 创建文件夹
     *
     * @param req 创建请求
     * @return 文件夹响应
     */
    KnowledgeFolderResp create(KnowledgeFolderCreateReq req);
    /**
     * 根据ID查询文件夹详情
     *
     * @param id 文件夹ID
     * @return 文件夹响应
     */
    KnowledgeFolderResp findDetail(Long id);
    /**
     * 获取知识库文件夹树
     *
     * @param knowledgeId 知识库ID
     * @return 文件夹树
     */
    List<KnowledgeFolderResp> getFolderTree(Long knowledgeId);

    /**
     * 根据知识库ID和父文件夹ID查询文件夹列表
     *
     * @param knowledgeId 知识库ID
     * @param parentId 父文件夹ID（0为根目录）
     * @return 文件夹列表
     */
    List<KnowledgeFolderResp> listByParentId(Long knowledgeId, Long parentId);
    /**
     * 更新文件夹
     *
     * @param id 文件夹ID
     * @param req 更新请求
     * @return 文件夹响应
     */
    KnowledgeFolderResp update(Long id, KnowledgeFolderUpdateReq req);

    /**
     * 删除文件夹
     *
     * @param id 文件夹ID
     */
    void delete(Long id);
}

