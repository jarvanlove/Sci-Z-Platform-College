package com.sciz.server.domain.pojo.dto.response.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目列表响应
 *
 * @param id                      Long 项目ID
 * @param number                  String 项目编号
 * @param name                    String 项目名称
 * @param description             String 项目描述
 * @param declarationId           Long 关联申报ID
 * @param budget                  BigDecimal 项目预算
 * @param progress                Integer 进度百分比
 * @param status                  String 项目状态
 * @param statusDescription       String 项目状态描述
 * @param startTime               LocalDate 项目开始时间（来自申报表）
 * @param estimatedCompletionTime LocalDate 预计完成时间（来自申报表）
 * @param projectLeader           String 项目负责人（来自申报表）
 * @param difyKnowledgeId         String Dify知识库ID
 * @param createdTime             LocalDateTime 创建时间
 * @param updatedTime             LocalDateTime 更新时间
 * @author JiaWen.Wu
 * @className ProjectListResp
 * @date 2025-11-24 16:00
 */
public record ProjectListResp(
                Long id,
                String number,
                String name,
                String description,
                Long declarationId,
                BigDecimal budget,
                Integer progress,
                String status,
                String statusDescription,
                LocalDate startTime,
                LocalDate estimatedCompletionTime,
                String projectLeader,
                String difyKnowledgeId,
                LocalDateTime createdTime,
                LocalDateTime updatedTime) {
}
