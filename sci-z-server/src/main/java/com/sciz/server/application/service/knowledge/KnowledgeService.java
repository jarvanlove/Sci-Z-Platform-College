package com.sciz.server.application.service.knowledge;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeChatbotStreamReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeCreateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 知识库应用服务
 * 
 * @author ShiHang.Shang
 * @className KnowledgeService
 * @date 2025-10-29 10:00
 */
public interface KnowledgeService {

    /**
     * 创建知识库
     *
     * @param req 创建请求
     * @return 知识库响应
     */
    KnowledgeResp create(KnowledgeCreateReq req);

    /**
     * 分页查询知识库列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 知识库分页结果
     */
    PageResult<KnowledgeResp> page(int page, int size);

    /**
     * 上传文件到知识库
     *
     * @param knowledgeId 知识库ID（Dify知识库ID，String类型）
     * @param file 上传的文件
     * @param folderId 文件夹ID（可选，0为根目录）
     * @return 是否上传成功
     */
    void uploadFile(String knowledgeId, MultipartFile file, Long folderId);

    /**
     * 基于知识库的 Chatbot 流式对话
     *
     * @param req 流式对话请求
     * @return 流式响应（SSE格式）
     */
    SseEmitter chatbotStream(KnowledgeChatbotStreamReq req);
}
