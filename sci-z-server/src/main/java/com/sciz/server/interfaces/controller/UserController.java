package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.application.service.log.OperationLogService;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.application.service.user.RolePermissionService;
import com.sciz.server.application.service.user.UserRoleService;
import com.sciz.server.application.service.user.UserService;
import com.sciz.server.domain.pojo.dto.request.system.RoleCreateReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleListQueryReq;
import com.sciz.server.domain.pojo.dto.request.system.RolePermissionUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.RoleUpdateReq;
import com.sciz.server.domain.pojo.dto.request.system.UserRoleUpdateReq;
import com.sciz.server.domain.pojo.dto.response.system.PermissionTreeResp;
import com.sciz.server.domain.pojo.dto.response.system.RoleResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryConfigResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldOptionResp;
import com.sciz.server.domain.pojo.dto.response.user.IndustryProfileFieldResp;
import com.sciz.server.domain.pojo.dto.response.user.RolePermissionIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.UserRoleIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.RoleListResp;
import com.sciz.server.domain.pojo.dto.response.user.RoleUserIdsResp;
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
import com.sciz.server.domain.pojo.dto.request.log.OperationLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.OperationLogResp;
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
    private final OperationLogService operationLogService;
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

    @Operation(summary = "分页查询角色列表", description = "分页查询角色列表，支持按关键字、状态筛选")
    @PostMapping("/roles/list")
    public Result<PageResult<RoleListResp>> pageRoles(@Valid @RequestBody RoleListQueryReq req) {
        var resp = userRoleService.page(req);
        return Result.success(resp);
    }

    @Operation(summary = "查询角色下的所有用户", description = "分页查询指定角色下的所有用户（仅查询当前行业下的用户），用于点击用户数量时显示")
    @PostMapping("/roles/{roleId}/users")
    public Result<PageResult<UserListResp>> listUsersByRoleId(@PathVariable Long roleId,
            @Valid @RequestBody UserListQueryReq req) {
        var resp = userRoleService.pageUsersByRoleId(roleId, req);
        return Result.success(resp);
    }

    @Operation(summary = "角色用户ID列表", description = "查询角色在当前行业下绑定的用户ID集合（用于绑定用户页面的回显：打开模态框时调用此接口获取已绑定的用户ID，然后在用户列表中勾选对应的用户。注意：绑定用户页面调用 /users/list 展示所有用户，然后使用此接口回显已绑定的用户）")
    @GetMapping("/roles/{roleId}/user-ids")
    public Result<RoleUserIdsResp> listRoleUserIds(@PathVariable Long roleId) {
        var userIds = userRoleService.listUserIdsByRoleId(roleId);
        var resp = userConverter.toRoleUserIdsResp(userIds);
        return Result.success(resp);
    }

    @Operation(summary = "创建角色", description = "创建角色")
    @PostMapping("/roles")
    public Result<RoleResp> createRole(@Valid @RequestBody RoleCreateReq req) {
        var resp = userRoleService.create(req);
        return Result.success(resp);
    }

    @Operation(summary = "更新角色", description = "更新角色")
    @PutMapping("/roles/{id}")
    public Result<RoleResp> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateReq req) {
        // 确保路径参数和请求体中的ID一致
        if (!id.equals(req.id())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "路径参数中的角色ID与请求体中的角色ID不一致");
        }
        var resp = userRoleService.update(req);
        return Result.success(resp);
    }

    @Operation(summary = "删除角色", description = "删除角色（系统角色不允许删除）")
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        userRoleService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "角色权限列表", description = "查询角色在当前行业下的权限ID集合（用于配置权限页面的回显：打开模态框时调用此接口获取已绑定的权限ID，然后在权限树中勾选对应的权限）")
    @GetMapping("/roles/{roleId}/permissions")
    public Result<RolePermissionIdsResp> listRolePermissionIds(@PathVariable Long roleId) {
        var permissionIds = rolePermissionService.listPermissionIds(roleId);
        var resp = userConverter.toRolePermissionIdsResp(permissionIds);
        return Result.success(resp);
    }

    @Operation(summary = "获取权限树", description = "获取权限树结构（用于角色权限配置）")
    @GetMapping("/permissions/tree")
    public Result<List<PermissionTreeResp>> getPermissionTree() {
        var resp = rolePermissionService.getPermissionTree();
        return Result.success(resp);
    }

    @Operation(summary = "更新角色权限", description = "全量替换角色在当前行业下绑定的权限集合。查询用户权限时会自动取所有角色的权限并集（去重），因此不同角色的权限可以重叠")
    @PostMapping("/roles/permissions")
    public Result<Void> updateRolePermissions(@Valid @RequestBody RolePermissionUpdateReq req) {
        rolePermissionService.updateRolePermissions(req);
        return Result.success();
    }

    @Operation(summary = "用户角色列表", description = "查询用户在当前行业下的角色ID集合（用于绑定用户到角色时，查询用户已有角色，然后合并新角色）")
    @GetMapping("/users/{userId}/roles")
    public Result<UserRoleIdsResp> listUserRoleIds(@PathVariable Long userId) {
        var industryType = industryConfigCache.get().getType();
        var roleIds = userRoleService.listUserRoleIds(userId, industryType);
        var resp = userConverter.toUserRoleIdsResp(roleIds);
        return Result.success(resp);
    }

    @Operation(summary = "获取角色列表", description = "查询角色列表（不分页）")
    @GetMapping("/roles")
    public Result<List<RoleListResp>> listRoles() {
        var roles = userRoleService.listRoles();
        return Result.success(roles);
    }

    @Operation(summary = "更新用户角色", description = "全量替换用户在当前行业下的角色集合（仅处理当前行业下的角色，不影响其他行业的角色）。支持绑定和解绑：前端需要传入用户最终应该绑定的所有角色ID列表，后端会自动添加新角色、删除不在列表中的已有角色。使用场景：1) 绑定用户到角色时，需要先查询用户已有角色，然后合并新角色后调用此接口；2) 解绑角色时，传入去除解绑角色后的完整角色列表")
    @PostMapping("/users/roles")
    public Result<Void> updateUserRoles(@Valid @RequestBody UserRoleUpdateReq req) {
        userRoleService.updateUserRoles(req);
        return Result.success();
    }

    @Operation(summary = "用户管理列表", description = "管理员分页查询用户列表，支持按关键字、角色、状态筛选")
    @PostMapping("/users/list")
    public Result<PageResult<UserListResp>> listUsers(@Valid @RequestBody UserListQueryReq req) {
        var resp = userService.page(req);
        return Result.success(resp);
    }

    @Operation(summary = "创建用户", description = "管理员创建用户，自动生成学工号")
    @PostMapping("/users/create")
    public Result<UserCreateResp> createUser(@Valid @RequestBody UserCreateReq req) {
        var resp = userService.create(req);
        return Result.success(resp);
    }

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

    @Operation(summary = "删除用户", description = "管理员软删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "禁用/启用用户", description = "管理员禁用或启用用户，true=禁用，false=启用")
    @PutMapping("/users/{id}/status")
    public Result<Void> disableUser(@PathVariable Long id, @RequestParam boolean disabled) {
        userService.disableById(id, disabled);
        return Result.success();
    }

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

    @Operation(summary = "操作日志", description = "系统操作日志分页查询")
    @PostMapping("/operation/logs")
    public Result<PageResult<OperationLogResp>> operationLogs(@Valid @RequestBody OperationLogQueryReq req) {
        var resp = operationLogService.page(req);
        return Result.success(resp);
    }

    @Operation(summary = "操作日志详情", description = "查询指定操作日志的详细信息")
    @GetMapping("/operation/logs/{logId}")
    public Result<OperationLogResp> operationLogDetail(@PathVariable Long logId) {
        var resp = operationLogService.findDetail(logId);
        return Result.success(resp);
    }
}
