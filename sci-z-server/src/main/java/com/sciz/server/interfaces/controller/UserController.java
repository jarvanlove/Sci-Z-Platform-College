package com.sciz.server.interfaces.controller;

import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JiaWen.Wu
 * @className UserController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户接口占位")
public class UserController {

    /**
     * 用户接口占位
     *
     * @param 
     * @return 占位返回
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("NOT_IMPLEMENTED");
    }
}


