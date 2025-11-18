package com.sciz.server.domain.pojo.dto.request.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

/**
 * 更新角色请求
 *
 * @param id          Long 角色ID
 * @param roleName    String 角色名称
 * @param description String 角色描述
 * @param sortOrder   Integer 排序值
 * @param status      Integer 角色状态（1=启用，0=禁用）
 *
 * @author JiaWen.Wu
 * @className RoleUpdateReq
 * @date 2025-11-17 14:00
 */
public record RoleUpdateReq(
        @NotNull(message = "角色ID不能为空") Long id,

        @NotBlank(message = "角色名称不能为空") @Size(max = 50, message = "角色名称长度不能超过50个字符") String roleName,

        @Size(max = 200, message = "角色描述长度不能超过200个字符") String description,

        Integer sortOrder,

        Integer status) {

    public RoleUpdateReq {
        roleName = normalize(roleName);
        description = normalize(description);
        sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.trim();
    }
}
