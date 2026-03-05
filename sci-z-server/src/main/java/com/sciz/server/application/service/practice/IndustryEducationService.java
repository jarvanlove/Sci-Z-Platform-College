package com.sciz.server.application.service.practice;

import com.sciz.server.domain.pojo.dto.request.practice.DistributeReq;
import com.sciz.server.domain.pojo.dto.request.practice.DistributeRejectReq;
import com.sciz.server.domain.pojo.dto.request.practice.IndustryEducationAssignReq;
import com.sciz.server.domain.pojo.dto.request.practice.TeamMatchReq;
import com.sciz.server.domain.pojo.dto.response.practice.TeamDetailResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamMatchResp;

import java.util.List;

/**
 * 产教研智能体应用服务
 * <p>
 * 申报课题匹配科研团队、团队明细、分发给目标团队；消息驱动的分发与接受/拒绝。
 * </p>
 *
 * @author Sci-Z
 */
public interface IndustryEducationService {

    /**
     * 根据申报课题关键词匹配科研团队（以项目为团队单元）
     *
     * @param req 匹配请求（keyword、limit）
     * @return 团队列表，最多 limit 条
     */
    List<TeamMatchResp> matchTeams(TeamMatchReq req);

    /**
     * 获取团队明细（成员、关联项目、效率/荣誉占位）
     *
     * @param teamId 团队ID（即 projectId）
     * @return 团队明细，不存在时返回 null
     */
    TeamDetailResp getTeamDetail(Long teamId);

    /**
     * 将申报分发给目标团队：创建新项目并绑定申报与目标团队的负责人及成员
     *
     * @param req 分发请求（declarationId、targetTeamId）
     * @return 新创建的项目ID
     */
    Long assign(IndustryEducationAssignReq req);

    /**
     * 消息驱动分发：领导将科研项目描述分发给目标团队负责人，发送站内消息并 WebSocket 推送
     *
     * @param req 分发请求（topicLabel、targetTeamId）
     * @return 站内消息ID
     */
    Long distribute(DistributeReq req);

    /**
     * 接受分发：仅调用申报创建接口创建申报，并通知分发人（与项目创建无关）
     *
     * @param messageId 站内消息ID
     * @return 新创建的申报ID
     */
    Long acceptDistribute(Long messageId);

    /**
     * 拒绝分发：填写拒绝原因并通知分发人
     *
     * @param messageId 站内消息ID
     * @param req        拒绝原因
     */
    void rejectDistribute(Long messageId, DistributeRejectReq req);
}
