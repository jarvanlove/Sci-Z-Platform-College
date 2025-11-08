package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 用户角色ID列表响应
 *
 * @param roleIdList List<Long> 角色ID集合
 * @author JiaWen.Wu
 * @className UserRoleIdsResp
 * @date 2025-11-09 03:05
 */
public record UserRoleIdsResp(List<Long> roleIdList) {
}
