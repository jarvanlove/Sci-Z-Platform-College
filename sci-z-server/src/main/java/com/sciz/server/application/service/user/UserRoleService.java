package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.system.RoleCreateReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleListQueryReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.system.RoleResp;
import com.sciz.server.domain.pojo.dto.response.user.RoleListResp;
import com.sciz.server.domain.pojo.dto.response.user.UserListResp;
import com.sciz.server.domain.pojo.dto.request.user.UserListQueryReq;
import com.sciz.server.infrastructure.shared.result.PageResult;
import java.util.List;

/**
 * 用户角色管理服务
 *
 * @author JiaWen.Wu
 * @className UserRoleService
 * @date 2025-11-09 02:10
 */
public interface UserRoleService {

    /**
     * 查询当前行业下的角色列表
     *
     * @return List<RoleListResp> 角色列表
     */
    List<RoleListResp> listRoles();

    /**
     * 分页查询角色列表
     *
     * @param req RoleListQueryReq 查询请求
     * @return PageResult<RoleListResp> 分页结果
     */
    PageResult<RoleListResp> page(RoleListQueryReq req);

    /**
     * 创建角色
     *
     * @param req RoleCreateReq 创建请求
     * @return RoleResp 角色响应
     */
    RoleResp create(RoleCreateReq req);

    /**
     * 更新角色
     *
     * @param req RoleUpdateReq 更新请求
     * @return RoleResp 角色响应
     */
    RoleResp update(RoleUpdateReq req);

    /**
     * 删除角色
     *
     * @param id Long 角色ID
     */
    void deleteById(Long id);

    /**
     * 查询用户在指定行业下的角色ID列表
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<Long> 角色ID集合
     */
    List<Long> listUserRoleIds(Long userId, String industryType);

    /**
     * 更新用户在指定行业下的角色集合（全量替换）
     *
     * @param req UserRoleUpdateReq 更新请求
     */
    void updateUserRoles(UserRoleUpdateReq req);

    /**
     * 分页查询角色下的所有用户
     *
     * @param roleId Long 角色ID
     * @param req    UserListQueryReq 查询请求（分页参数）
     * @return PageResult<UserListResp> 分页结果
     */
    PageResult<UserListResp> pageUsersByRoleId(Long roleId, UserListQueryReq req);

    /**
     * 查询角色在当前行业下绑定的用户ID列表（用于绑定用户页面的回显）
     *
     * @param roleId Long 角色ID
     * @return List<Long> 用户ID集合
     */
    List<Long> listUserIdsByRoleId(Long roleId);
}
