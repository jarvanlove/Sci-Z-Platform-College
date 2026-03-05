package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.practice.IndustryEducationService;
import com.sciz.server.domain.pojo.dto.request.practice.DistributeReq;
import com.sciz.server.domain.pojo.dto.request.practice.DistributeRejectReq;
import com.sciz.server.domain.pojo.dto.request.practice.IndustryEducationAssignReq;
import com.sciz.server.domain.pojo.dto.request.practice.TeamMatchReq;
import com.sciz.server.domain.pojo.dto.response.practice.TeamDetailResp;
import com.sciz.server.domain.pojo.dto.response.practice.TeamMatchResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产教研智能体控制器
 * <p>
 * 申报课题匹配科研团队、团队明细、分发给目标团队。权限由前端/菜单控制，接口暂不做 Sa-Token 权限校验。
 * </p>
 *
 * @author Sci-Z
 */
@Tag(name = "产教研智能体", description = "申报课题匹配团队、团队明细、分发")
@RestController
@RequestMapping("/api/industry-education")
@RequiredArgsConstructor
public class IndustryEducationController {

    private final IndustryEducationService industryEducationService;

    @Operation(summary = "匹配团队", description = "根据申报课题关键词匹配科研团队，以项目为团队单元")
    @PostMapping("/teams/match")
    public Result<List<TeamMatchResp>> matchTeams(@Valid @RequestBody TeamMatchReq req) {
        List<TeamMatchResp> list = industryEducationService.matchTeams(req);
        return Result.success(list);
    }

    @Operation(summary = "团队明细", description = "根据团队ID（projectId）获取团队详情：成员、项目、效率/荣誉占位")
    @GetMapping("/teams/{id}")
    public Result<TeamDetailResp> getTeamDetail(@PathVariable("id") Long id) {
        TeamDetailResp detail = industryEducationService.getTeamDetail(id);
        return Result.success(detail);
    }

    /**
     * 1. 直接分发 assign
     * 逻辑：已有申报时，将申报分发给目标团队并立即创建新项目（绑定申报与目标团队负责人/成员）。
     * 什么时候调用：当业务上需要「不经过消息、直接把已有申报挂到目标团队并建项目」时调用（如从申报列表选申报+选团队一键分发）。
     * 注意：前端「分发科研项目」弹窗不调用本接口，应调用 2.distribute。
     */
    @Operation(summary = "直接分发", description = "将已有申报分发给目标团队并立即创建项目")
    @PostMapping("/assign")
    public Result<Long> assign(@Valid @RequestBody IndustryEducationAssignReq req) {
        Long projectId = industryEducationService.assign(req);
        return Result.success(projectId);
    }

    /**
     * 2. 消息驱动分发 distribute
     * 逻辑：将表单中的申报基础信息写入站内消息并 WebSocket 推送给目标团队负责人；与项目创建无关，仅与申报有关。
     * 什么时候调用：领导在产教研页点击「分发给该团队」→ 弹出「分发科研项目」弹窗 → 填写表单 → 点击「确定」时调用（前端：IndustryEducation.vue confirmAssign → api distribute）。
     */
    @Operation(summary = "消息驱动分发", description = "发站内消息给负责人，接受后按表单信息仅创建申报")
    @PostMapping("/distribute")
    public Result<Long> distribute(@Valid @RequestBody DistributeReq req) {
        Long messageId = industryEducationService.distribute(req);
        return Result.success(messageId);
    }

    /**
     * 3. 接受分发 /distribute/{messageId}/accept
     * 逻辑：被分发人确认接受后，仅根据消息中的申报基础信息调用「创建申报」接口创建申报，并给下发领导发结果通知（与项目创建无关）。
     * 什么时候调用：被分发人登录 → 进入「系统消息」页 → 点开一条「科研项目分发」消息 → 点击「接受」时调用（前端：Messages.vue handleAccept → acceptDistribute(messageId)）。
     */
    @Operation(summary = "接受分发", description = "接受科研项目分发，仅创建申报并通知分发人")
    @PostMapping("/distribute/{messageId}/accept")
    public Result<Long> acceptDistribute(@PathVariable Long messageId) {
        Long projectId = industryEducationService.acceptDistribute(messageId);
        return Result.success(projectId);
    }

    @Operation(summary = "拒绝分发", description = "拒绝科研项目分发，填写原因并通知分发人")
    @PostMapping("/distribute/{messageId}/reject")
    public Result<Void> rejectDistribute(@PathVariable Long messageId, @Valid @RequestBody DistributeRejectReq req) {
        industryEducationService.rejectDistribute(messageId, req);
        return Result.success();
    }
}
