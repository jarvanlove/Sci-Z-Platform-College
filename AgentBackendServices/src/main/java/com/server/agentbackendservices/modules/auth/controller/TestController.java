package com.server.agentbackendservices.modules.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于测试异常处理功能
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Tag(name = "测试接口", description = "测试异常处理功能")
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 测试需要登录的接口
     */
    @GetMapping("/login-required")
    @Operation(summary = "测试登录验证", description = "测试未登录时是否返回异常")
    public ResultVO<String> testLoginRequired() {
        // 这里会自动触发 Sa-Token 的登录检查
        StpUtil.checkLogin();
        return ResultVO.ok("登录验证通过", "您已成功登录");
    }

    /**
     * 测试业务异常
     */
    @GetMapping("/business-exception")
    @Operation(summary = "测试业务异常", description = "测试业务异常处理")
    public ResultVO<String> testBusinessException() {
        throw new com.server.agentbackendservices.modules.common.exception.BusinessException(
            com.server.agentbackendservices.modules.common.enums.StatusCodeEnum.FAIL, 
            "这是一个测试业务异常"
        );
    }

    /**
     * 测试系统异常
     */
    @GetMapping("/system-exception")
    @Operation(summary = "测试系统异常", description = "测试系统异常处理")
    public ResultVO<String> testSystemException() {
        throw new RuntimeException("这是一个测试系统异常");
    }
}
