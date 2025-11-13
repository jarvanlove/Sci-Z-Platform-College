package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
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
}
