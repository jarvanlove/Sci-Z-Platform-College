package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.user.UserAdminResetPasswordReq;
import com.sciz.server.domain.pojo.dto.request.user.UserCreateReq;
import com.sciz.server.domain.pojo.dto.request.user.UserListQueryReq;
import com.sciz.server.domain.pojo.dto.request.user.UserUpdateReq;
import com.sciz.server.domain.pojo.dto.response.user.UserCreateResp;
import com.sciz.server.domain.pojo.dto.response.user.UserListResp;
import com.sciz.server.domain.pojo.dto.response.user.UserUpdateResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 用户应用服务
 * 
 * @author JiaWen.Wu
 * @className UserService
 * @date 2025-10-28 00:00
 */
public interface UserService {

    /**
     * 获取用户信息
     *
     * @param userId Long 用户ID
     * @return Object 用户信息
     */
    Object getUser(Long userId);

    /**
     * 创建用户
     * 管理员创建用户，自动生成学工号
     *
     * @param req UserCreateReq 创建请求
     * @return UserCreateResp 创建响应
     */
    UserCreateResp create(UserCreateReq req);

    /**
     * 分页查询用户列表
     *
     * @param req UserListQueryReq 查询请求
     * @return PageResult<UserListResp> 分页结果
     */
    PageResult<UserListResp> page(UserListQueryReq req);

    /**
     * 更新用户
     * 管理员更新用户信息
     *
     * @param req UserUpdateReq 更新请求
     * @return UserUpdateResp 更新响应
     */
    UserUpdateResp update(UserUpdateReq req);

    /**
     * 删除用户
     * 管理员软删除用户
     *
     * @param userId Long 用户ID
     */
    void deleteById(Long userId);

    /**
     * 禁用/启用用户
     * 管理员禁用或启用用户
     *
     * @param userId   Long 用户ID
     * @param disabled boolean true=禁用，false=启用
     */
    void disableById(Long userId, boolean disabled);

    /**
     * 管理员重置用户密码
     * 管理员重置指定用户的密码，不需要验证码
     *
     * @param req UserAdminResetPasswordReq 重置密码请求
     */
    void adminResetPassword(UserAdminResetPasswordReq req);
}
