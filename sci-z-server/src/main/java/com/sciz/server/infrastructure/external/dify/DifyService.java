package com.sciz.server.infrastructure.external.dify;

/**
 * Dify 外部服务接口
 * 定义与 Dify 的能力边界，供 application/ 与 handler/ 通过依赖注入调用。
 *
 *
 * @author JiaWen.Wu
 * @className DifyService
 * @date 2025-10-29 10:00
 */
public interface DifyService {

    /**
     * 调用 Dify 应用进行对话/补全
     *
     * @param appId  Dify 应用 ID
     * @param userId 业务用户 ID（用于会话隔离/审计）
     * @param input  用户输入
     * @return Dify 生成的文本
     */
    String invokeChat(String appId, String userId, String input);

    /**
     * 运行 Dify 工作流
     *
     * @param workflowId 工作流 ID
     * @param payload    运行所需的 JSON 字符串（可用 Map 序列化后传入）
     * @return 工作流运行的输出 JSON 字符串
     */
    String runWorkflow(String workflowId, String payload);

    /**
     * 同步知识库文档
     *
     * @param knowledgeBaseId 知识库 ID
     * @param documentId      文档 ID（业务侧标识）
     * @param content         文本内容
     */
    void syncKnowledge(String knowledgeBaseId, String documentId, String content);
}
