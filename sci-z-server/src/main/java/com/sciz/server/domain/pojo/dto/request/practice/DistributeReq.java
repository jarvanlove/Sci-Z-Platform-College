package com.sciz.server.domain.pojo.dto.request.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 产教研分发请求（消息驱动）
 * <p>
 * 领导将科研项目描述及申报基础信息分发给目标团队负责人；负责人接受后按此信息创建申报。
 * </p>
 *
 * @param topicLabel           科研项目描述/研究课题（必填）
 * @param targetTeamId         目标团队ID（即 projectId，负责人将收到消息）
 * @param department           课题发布部门（可选，接受时写入申报）
 * @param documentPublishTime  红头文件发布时间（可选）
 * @param projectStartTime     项目开始时间（可选）
 * @param projectEndTime       项目结束时间（可选）
 * @param researchTopic       研究课题（可选，缺省用 topicLabel）
 * @param researchDirection   研究方向（可选）
 * @param researchFields      研究领域列表（可选，最多 10 项）
 */
public record DistributeReq(
        @NotBlank(message = "科研项目描述不能为空") String topicLabel,
        @NotNull(message = "目标团队ID不能为空") Long targetTeamId,
        String department,
        LocalDate documentPublishTime,
        LocalDate projectStartTime,
        LocalDate projectEndTime,
        String researchTopic,
        String researchDirection,
        List<String> researchFields) {

    public DistributeReq(String topicLabel, Long targetTeamId) {
        this(topicLabel, targetTeamId, null, null, null, null, null, null, null);
    }
}
