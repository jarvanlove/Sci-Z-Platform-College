package com.sciz.server.domain.pojo.dto.response.project;

/**
 * 项目下拉框响应
 *
 * @param id                 Long 项目ID
 * @param number             String 项目编号
 * @param name               String 项目名称
 * @param statusDescription  String 项目状态描述
 * @param documentCount      Integer 文档数量（来自 sys_knowledge_base.file_count）
 * @param totalWords         Long 文档总字数（通过 sys_knowledge_file_relation →
 *                           sys_attachment 统计 file_size，转换为字数）
 * @param totalDownloadCount Long 下载总次数（通过 sys_knowledge_file_relation →
 *                           sys_attachment 统计 download_count）
 * @param progress           Integer 项目进度（百分比）
 * @author JiaWen.Wu
 * @className ProjectSelectResp
 * @date 2025-12-12 16:00
 */
public record ProjectSelectResp(
        Long id,
        String number,
        String name,
        String statusDescription,
        Integer documentCount,
        Long totalWords,
        Long totalDownloadCount,
        Integer progress,
        String dify_knowledge_id

) {
}
