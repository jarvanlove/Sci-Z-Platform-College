package com.sciz.server.domain.pojo.dto.response.system;

import java.util.List;

/**
 * 权限树节点响应
 *
 * @param id             Long 权限ID
 * @param parentId       Long 父权限ID（0表示顶级）
 * @param permissionName String 权限名称
 * @param permissionCode String 权限编码
 * @param permissionType Integer 权限类型（1=菜单，2=按钮，3=API）
 * @param path           String 路由路径
 * @param icon           String 图标
 * @param sortOrder      Integer 排序值
 * @param status         Integer 权限状态（1=启用，0=禁用）
 * @param children       List<PermissionTreeResp> 子权限列表
 *
 * @author JiaWen.Wu
 * @className PermissionTreeResp
 * @date 2025-11-17 14:00
 */
public record PermissionTreeResp(
        Long id,
        Long parentId,
        String permissionName,
        String permissionCode,
        Integer permissionType,
        String path,
        String icon,
        Integer sortOrder,
        Integer status,
        List<PermissionTreeResp> children) {
}
