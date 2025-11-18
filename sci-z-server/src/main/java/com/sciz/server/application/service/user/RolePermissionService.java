package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.system.RolePermissionUpdateReq;
import com.sciz.server.domain.pojo.dto.response.system.PermissionTreeResp;
import java.util.List;

/**
 * 角色权限管理服务
 *
 * @author JiaWen.Wu
 * @className RolePermissionService
 * @date 2025-11-09 02:10
 */
public interface RolePermissionService {

    /**
     * 查询角色的权限ID列表
     *
     * @param roleId       Long 角色ID
     * @param industryType String 行业类型
     * @return List<Long> 权限ID列表
     */
    List<Long> listPermissionIds(Long roleId);

    /**
     * 更新角色权限（全量替换）
     *
     * @param req RolePermissionUpdateReq 更新请求
     */
    void updateRolePermissions(RolePermissionUpdateReq req);

    /**
     * 获取权限树
     *
     * @return List<PermissionTreeResp> 权限树
     */
    List<PermissionTreeResp> getPermissionTree();
}
