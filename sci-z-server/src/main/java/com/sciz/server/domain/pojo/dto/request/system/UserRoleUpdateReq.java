package com.sciz.server.domain.pojo.dto.request.system;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户角色更新请求
 *
 * @param userId     Long 用户ID
 * @param roleIdList List<Long> 角色ID集合
 * @author JiaWen.Wu
 * @className UserRoleUpdateReq
 * @date 2025-11-09 02:10
 */
public record UserRoleUpdateReq(
                @NotNull(message = "用户ID不能为空") Long userId,
                @NotEmpty(message = "角色列表不能为空") List<Long> roleIdList) {
}
