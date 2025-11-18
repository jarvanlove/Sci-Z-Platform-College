package com.sciz.server.domain.pojo.dto.request.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

/**
 * 创建角色请求
 *
 * @param roleName    String 角色名称
 * @param roleCode    String 角色编码（唯一）
 * @param description String 角色描述
 * @param sortOrder   Integer 排序值
 *
 * @author JiaWen.Wu
 * @className RoleCreateReq
 * @date 2025-11-17 14:00
 */
public record RoleCreateReq(
        @NotBlank(message = "角色名称不能为空") @Size(max = 50, message = "角色名称长度不能超过50个字符") String roleName,

        @NotBlank(message = "角色编码不能为空") @Size(max = 50, message = "角色编码长度不能超过50个字符") String roleCode,

        @Size(max = 200, message = "角色描述长度不能超过200个字符") String description,

        Integer sortOrder) {

    public RoleCreateReq {
        roleName = normalize(roleName);
        roleCode = normalize(roleCode);
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
