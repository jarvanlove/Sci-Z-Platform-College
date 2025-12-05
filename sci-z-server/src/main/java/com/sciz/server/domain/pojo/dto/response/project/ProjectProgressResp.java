package com.sciz.server.domain.pojo.dto.response.project;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目进度响应
 *
 * @param projectInfo     ProjectBasicInfo 项目基本信息
 * @param overallProgress OverallProgress 整体进度
 * @param milestones      List<MilestoneProgressResp> 里程碑列表（时间轴视图）
 * @author JiaWen.Wu
 * @className ProjectProgressResp
 * @date 2025-12-04 19:00
 */
public record ProjectProgressResp(
                ProjectBasicInfo projectInfo,
                OverallProgress overallProgress,
                List<MilestoneProgressResp> milestones) {

        /**
         * 项目基本信息
         *
         * @param projectName    String 项目名称
         * @param projectNumber  String 项目编号
         * @param projectManager String 项目负责人
         * @param projectStatus  String 项目状态
         */
        public record ProjectBasicInfo(
                        String projectName,
                        String projectNumber,
                        String projectManager,
                        String projectStatus) {
        }

        /**
         * 整体进度
         *
         * @param percentage          Integer 进度百分比
         * @param startDate           LocalDate 开始时间
         * @param estimatedCompletion LocalDate 预计完成时间
         */
        public record OverallProgress(
                        Integer percentage,
                        LocalDate startDate,
                        LocalDate estimatedCompletion) {
        }

        /**
         * 里程碑进度响应
         *
         * @param id          Long 里程碑ID
         * @param title       String 里程碑标题
         * @param description String 描述
         * @param startTime   LocalDate 开始时间
         * @param endTime     LocalDate 结束时间
         * @param progress    Integer 进度百分比
         * @param status      String 状态（已完成、进行中、未开始、已延期）
         */
        public record MilestoneProgressResp(
                        Long id,
                        String title,
                        String description,
                        LocalDate startTime,
                        LocalDate endTime,
                        Integer progress,
                        String status) {
        }
}
