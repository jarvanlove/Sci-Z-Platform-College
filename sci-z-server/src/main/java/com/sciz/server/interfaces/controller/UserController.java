package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.user.RolePermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.domain.pojo.dto.request.system.RolePermissionUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.user.IndustryConfigResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldOptionResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldResp;
import com.sciz.server.domain.pojo.dto.response.user.RolePermissionIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.UserRoleIdsResp;
import com.sciz.server.interfaces.converter.UserConverter;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户与权限管理接口
 *
 * @author JiaWen.Wu
 * @className UserController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "行业配置、部门、角色、权限、用户、日志")
@RequiredArgsConstructor
public class UserController {

    private final UserRoleService userRoleService;
    private final RolePermissionService rolePermissionService;
    private final UserConverter userConverter;
    private final IndustryConfigCache industryConfigCache;

    @Operation(summary = "获取行业配置", description = "查询当前行业配置")
    @GetMapping("/industry/config")
    public Result<IndustryConfigResp> getIndustryConfig() {
        var view = industryConfigCache.get();
        var profileFieldRespList = Optional.ofNullable(view.getProfileFields())
                .orElseGet(List::of)
                .stream()
                .map(field -> new IndustryProfileFieldResp(
                        field.getFieldCode(),
                        field.getFieldLabel(),
                        field.getFieldType(),
                        field.getIsRequired(),
                        Optional.ofNullable(field.getOptions())
                                .orElseGet(List::of)
                                .stream()
                                .map(option -> new IndustryProfileFieldOptionResp(
                                        option.getOptionValue(),
                                        option.getOptionLabel(),
                                        option.getIsDefault()))
                                .toList()))
                .toList();
        var resp = new IndustryConfigResp(
                view.getType(),
                view.getName(),
                view.getDepartmentLabel(),
                view.getRoleLabel(),
                view.getEmployeeIdLabel(),
                profileFieldRespList);
        return Result.success(resp);
    }

    /**
     * 行业扩展字段
     *
     * @return Result<List<IndustryProfileFieldResp>> 扩展字段配置
     */
    @Operation(summary = "行业扩展字段", description = "获取当前行业下的用户扩展字段列表（含选项）")
    @GetMapping("/profile/fields")
    public Result<List<IndustryProfileFieldResp>> listProfileFields() {
        var view = industryConfigCache.get();
        var respList = Optional.ofNullable(view.getProfileFields())
                .orElseGet(List::of)
                .stream()
                .map(field -> new IndustryProfileFieldResp(
                        field.getFieldCode(),
                        field.getFieldLabel(),
                        field.getFieldType(),
                        field.getIsRequired(),
                        Optional.ofNullable(field.getOptions())
                                .orElseGet(List::of)
                                .stream()
                                .map(option -> new IndustryProfileFieldOptionResp(
                                        option.getOptionValue(),
                                        option.getOptionLabel(),
                                        option.getIsDefault()))
                                .toList()))
                .toList();
        return Result.success(respList);
    }

    @Operation(summary = "更新行业配置", description = "更新行业配置")
    @PutMapping("/industry/config")
    public Result<Object> updateIndustryConfig(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "获取部门列表", description = "按行业查询部门列表")
    @GetMapping("/departments")
    public Result<Object> listDepartments(@RequestParam(required = false) String industryType) {
        return Result.success(null);
    }

    @Operation(summary = "创建部门", description = "创建部门")
    @PostMapping("/departments")
    public Result<Object> createDepartment(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "更新部门", description = "更新部门")
    @PutMapping("/departments/{id}")
    public Result<Object> updateDepartment(@PathVariable Long id, @RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "删除部门", description = "删除部门")
    @DeleteMapping("/departments/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        return Result.success(null);
    }

    // 角色
    @Operation(summary = "获取角色列表", description = "查询角色列表")
    @GetMapping("/roles")
    public Result<Object> listRoles() {
        return Result.success(null);
    }

    @Operation(summary = "创建角色", description = "创建角色")
    @PostMapping("/roles")
    public Result<Object> createRole(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "更新角色", description = "更新角色")
    @PutMapping("/roles/{id}")
    public Result<Object> updateRole(@PathVariable Long id, @RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "删除角色", description = "删除角色")
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        return Result.success(null);
    }

    /**
     * 查询角色权限ID集合
     *
     * @param roleId       Long 角色ID
     * @param industryType String 行业类型
     * @return Result<List<Long>> 权限ID集合
     */
    @Operation(summary = "角色权限列表", description = "查询角色在指定行业下的权限ID集合")
    @GetMapping("/roles/{roleId}/permissions")
    public Result<RolePermissionIdsResp> listRolePermissionIds(@PathVariable Long roleId,
            @RequestParam String industryType) {
        var permissionIds = rolePermissionService.listPermissionIds(roleId, industryType);
        var resp = userConverter.toRolePermissionIdsResp(permissionIds);
        return Result.success(resp);
    }

    /**
     * 更新角色权限
     *
     * @param req RolePermissionUpdateReq 更新请求
     * @return Result<Void> 空结果
     */
    @Operation(summary = "更新角色权限", description = "全量替换角色在指定行业下绑定的权限集合")
    @PostMapping("/roles/permissions")
    public Result<Void> updateRolePermissions(@Valid @RequestBody RolePermissionUpdateReq req) {
        rolePermissionService.updateRolePermissions(req);
        return Result.success();
    }

    // 权限
    @Operation(summary = "获取权限树", description = "获取权限树结构")
    @GetMapping("/permissions/tree")
    public Result<Object> getPermissionTree(@RequestParam(required = false) String industryType) {
        return Result.success(null);
    }

    @Operation(summary = "创建权限", description = "创建权限")
    @PostMapping("/permissions")
    public Result<Object> createPermission(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "更新权限", description = "更新权限")
    @PutMapping("/permissions/{id}")
    public Result<Object> updatePermission(@PathVariable Long id, @RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "删除权限", description = "删除权限")
    @DeleteMapping("/permissions/{id}")
    public Result<Void> deletePermission(@PathVariable Long id) {
        return Result.success(null);
    }

    // 用户（管理视角）
    /**
     * 查询用户角色ID集合
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return Result<List<Long>> 角色ID集合
     */
    @Operation(summary = "用户角色列表", description = "查询用户在指定行业下的角色ID集合")
    @GetMapping("/users/{userId}/roles")
    public Result<UserRoleIdsResp> listUserRoleIds(@PathVariable Long userId,
            @RequestParam String industryType) {
        var roleIds = userRoleService.listUserRoleIds(userId, industryType);
        var resp = userConverter.toUserRoleIdsResp(roleIds);
        return Result.success(resp);
    }

    /**
     * 更新用户角色
     *
     * @param req UserRoleUpdateReq 更新请求
     * @return Result<Void> 空结果
     */
    @Operation(summary = "更新用户角色", description = "全量替换用户在指定行业下的角色集合")
    @PostMapping("/users/roles")
    public Result<Void> updateUserRoles(@Valid @RequestBody UserRoleUpdateReq req) {
        userRoleService.updateUserRoles(req);
        return Result.success();
    }

    @Operation(summary = "用户管理列表", description = "管理员查询用户列表")
    @GetMapping("/users")
    public Result<Object> listUsers() {
        return Result.success(null);
    }

    @Operation(summary = "创建用户", description = "管理员创建用户")
    @PostMapping("/users")
    public Result<Object> createUser(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "更新用户", description = "管理员更新用户")
    @PutMapping("/users/{id}")
    public Result<Object> updateUser(@PathVariable Long id, @RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "删除用户", description = "管理员删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        return Result.success(null);
    }

    // 日志
    @Operation(summary = "登录日志", description = "查询登录日志")
    @GetMapping("/logs/login")
    public Result<Object> loginLogs(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(null);
    }

    @Operation(summary = "操作日志", description = "查询操作日志")
    @GetMapping("/logs/operation")
    public Result<Object> operationLogs(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(null);
    }

}
