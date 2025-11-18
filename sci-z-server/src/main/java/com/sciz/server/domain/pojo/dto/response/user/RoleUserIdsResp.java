package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 角色用户ID列表响应
 *
 * @param userIdList List<Long> 用户ID集合
 * @author JiaWen.Wu
 * @className RoleUserIdsResp
 * @date 2025-11-18 15:00
 */
public record RoleUserIdsResp(List<Long> userIdList) {
}
