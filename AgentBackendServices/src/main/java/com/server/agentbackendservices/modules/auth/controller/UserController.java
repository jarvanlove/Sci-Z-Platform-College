package com.server.agentbackendservices.modules.auth.controller;

import com.server.agentbackendservices.modules.auth.entity.User;
import com.server.agentbackendservices.modules.auth.mapper.UserMapper;
import com.server.agentbackendservices.modules.common.controller.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 提供用户相关的增删改查接口
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "用户相关的增删改查操作")
@RestController
@RequestMapping("/users")
public class UserController extends BaseController<UserMapper, User> {
    /**
     * 获取所有用户列表 (GET方法)
     * @return 用户列表
     */
//    @GetMapping("/list")
//    @Operation(summary = "获取所有用户列表", description = "获取所有未删除的用户列表")
//    public ResultVO<List<User>> getAllUsers() {
//        try {
//            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(User::getDeleted, 0); // 只查询未删除的用户
//            queryWrapper.orderByDesc(User::getCreateTime); // 按创建时间倒序
//            List<User> userList = userService.list(queryWrapper);
//            return ResultVO.ok(userList, "获取用户列表成功");
//        } catch (Exception e) {
//            return ResultVO.fail("获取用户列表失败: " + e.getMessage());
//        }
//    }

}
