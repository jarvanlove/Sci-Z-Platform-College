package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.application.service.user.RolePermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.application.service.user.UserService;
import com.sciz.server.domain.pojo.dto.request.system.RolePermissionUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.user.IndustryConfigResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldOptionResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldResp;
import com.sciz.server.domain.pojo.dto.response.user.RolePermissionIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.UserRoleIdsResp;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.request.user.UserAdminResetPasswordReq;
import com.sciz.server.domain.pojo.dto.request.user.UserCreateReq;
import com.sciz.server.domain.pojo.dto.request.user.UserInfoUpdateReq;
import com.sciz.server.domain.pojo.dto.request.user.UserListQueryReq;
import com.sciz.server.domain.pojo.dto.request.user.UserUpdateReq;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.user.UserCreateResp;
import com.sciz.server.domain.pojo.dto.response.user.UserInfoUpdateResp;
import com.sciz.server.domain.pojo.dto.response.user.UserListResp;
import com.sciz.server.domain.pojo.dto.response.user.UserUpdateResp;
import com.sciz.server.domain.pojo.dto.request.log.LoginLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.LoginLogResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.interfaces.converter.UserConverter;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
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
import org.springframework.web.bind.annotation.ModelAttribute;

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
    private final LoginLogService loginLogService;
    private final AuthService authService;
    private final UserService userService;

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

    /**
     * 分页查询用户列表
     *
     * @param req UserListQueryReq 查询请求
     * @return Result<PageResult<UserListResp>> 分页结果
     */
    @Operation(summary = "用户管理列表", description = "管理员分页查询用户列表，支持按关键字、角色、状态筛选")
    @PostMapping("/users/list")
    public Result<PageResult<UserListResp>> listUsers(@Valid @RequestBody UserListQueryReq req) {
        var resp = userService.page(req);
        return Result.success(resp);
    }

    /**
     * 创建用户
     *
     * @param req UserCreateReq 创建请求
     * @return Result<UserCreateResp> 创建响应
     */
    @Operation(summary = "创建用户", description = "管理员创建用户，自动生成学工号")
    @PostMapping("/users")
    public Result<UserCreateResp> createUser(@Valid @RequestBody UserCreateReq req) {
        var resp = userService.create(req);
        return Result.success(resp);
    }

    /**
     * 更新用户
     *
     * @param id  Long 用户ID
     * @param req UserUpdateReq 更新请求
     * @return Result<UserUpdateResp> 更新响应
     */
    @Operation(summary = "更新用户", description = "管理员更新用户信息（真实姓名、邮箱、手机号、部门、行业类型）")
    @PutMapping("/users/{id}")
    public Result<UserUpdateResp> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateReq req) {
        // 确保路径参数和请求体中的ID一致
        if (!id.equals(req.id())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "路径参数中的用户ID与请求体中的用户ID不一致");
        }
        var resp = userService.update(req);
        return Result.success(resp);
    }

    /**
     * 删除用户
     *
     * @param id Long 用户ID
     * @return Result<Void> 空结果
     */
    @Operation(summary = "删除用户", description = "管理员软删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 禁用/启用用户
     *
     * @param id       Long 用户ID
     * @param disabled boolean true=禁用，false=启用
     * @return Result<Void> 空结果
     */
    @Operation(summary = "禁用/启用用户", description = "管理员禁用或启用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> disableUser(@PathVariable Long id, @RequestParam boolean disabled) {
        userService.disableById(id, disabled);
        return Result.success();
    }

    /**
     * 管理员重置用户密码
     *
     * @param req UserAdminResetPasswordReq 重置密码请求
     * @return Result<Void> 空结果
     */
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码，不需要验证码")
    @PutMapping("/users/password/reset")
    public Result<Void> adminResetPassword(@Valid @RequestBody UserAdminResetPasswordReq req) {
        userService.adminResetPassword(req);
        return Result.success();
    }

    @Operation(summary = "更新个人信息", description = "更新当前登录用户的基础资料信息（真实姓名、邮箱、手机号、部门、职称），不包含头像")
    @PutMapping("/info")
    public Result<UserInfoUpdateResp> updateUserInfo(@Valid @RequestBody UserInfoUpdateReq req) {
        var resp = authService.updateUserInfo(req);
        return Result.success(resp);
    }

    @Operation(summary = "上传用户头像", description = "上传用户头像文件，上传成功后自动更新用户头像URL")
    @PostMapping("/avatar")
    public Result<FileInfoResp> uploadAvatar(@Valid @ModelAttribute FileUploadReq req) {
        var resp = authService.uploadAvatar(req);
        return Result.success(resp, ResultCode.FILE_UPLOAD_SUCCESS.getMessage());
    }

    @Operation(summary = "登录日志", description = "分页查询当前登录用户的登录日志，支持按状态、日期范围筛选")
    @PostMapping("/login/logs")
    public Result<PageResult<LoginLogResp>> loginLogs(@Valid @RequestBody LoginLogQueryReq req) {
        var resp = loginLogService.page(req);
        return Result.success(resp);
    }

    @Operation(summary = "操作日志", description = "查询操作日志")
    @PostMapping("/operation/logs")
    public Result<Object> operationLogs(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(null);
    }

}
