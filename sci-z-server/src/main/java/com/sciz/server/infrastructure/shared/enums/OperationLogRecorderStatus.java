package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 操作类型枚举
 * 按业务模块划分，统一管理操作名称和详细描述，便于维护
 * key：操作名称（用于操作日志的operation字段）
 * value：详细描述（用于操作日志的detail字段，用户友好格式）
 *
 * @author JiaWen.Wu
 * @className OperationType
 * @date 2025-11-17 15:30
 */
@Getter
public enum OperationLogRecorderStatus {

    // ==================== 用户管理模块 ====================

    /**
     * 创建用户
     */
    USER_CREATE("创建用户", "已创建用户"),

    /**
     * 更新用户
     */
    USER_UPDATE("更新用户", "已更新用户"),

    /**
     * 删除用户
     */
    USER_DELETE("删除用户", "已删除用户"),

    /**
     * 禁用用户
     */
    USER_DISABLE("禁用用户", "已禁用用户"),

    /**
     * 启用用户
     */
    USER_ENABLE("启用用户", "已启用用户"),

    /**
     * 重置用户密码
     */
    USER_RESET_PASSWORD("重置用户密码", "已重置用户密码"),

    /**
     * 更新用户信息
     */
    USER_UPDATE_INFO("更新用户信息", "已更新用户信息"),

    /**
     * 上传用户头像
     */
    USER_UPLOAD_AVATAR("上传用户头像", "已上传用户头像"),

    // ==================== 角色权限模块 ====================

    /**
     * 创建角色
     */
    ROLE_CREATE("创建角色", "已创建角色"),

    /**
     * 更新角色
     */
    ROLE_UPDATE("更新角色", "已更新角色"),

    /**
     * 删除角色
     */
    ROLE_DELETE("删除角色", "已删除角色"),

    /**
     * 更新角色权限
     */
    ROLE_UPDATE_PERMISSIONS("更新角色权限", "已更新角色权限"),

    /**
     * 更新用户角色
     */
    USER_UPDATE_ROLES("更新用户角色", "已更新用户角色"),

    // ==================== 权限管理模块 ====================

    /**
     * 创建权限
     */
    PERMISSION_CREATE("创建权限", "已创建权限"),

    /**
     * 更新权限
     */
    PERMISSION_UPDATE("更新权限", "已更新权限"),

    /**
     * 删除权限
     */
    PERMISSION_DELETE("删除权限", "已删除权限"),

    // ==================== 项目管理模块 ====================

    /**
     * 创建项目
     */
    PROJECT_CREATE("创建项目", "已创建项目"),

    /**
     * 更新项目
     */
    PROJECT_UPDATE("更新项目", "已更新项目"),

    /**
     * 删除项目
     */
    PROJECT_DELETE("删除项目", "已删除项目"),

    /**
     * 提交项目
     */
    PROJECT_SUBMIT("提交项目", "已提交项目"),

    /**
     * 审核项目
     */
    PROJECT_APPROVE("审核项目", "已审核项目"),

    // ==================== 申报管理模块 ====================

    /**
     * 创建申报
     */
    DECLARATION_CREATE("创建申报", "已创建申报"),

    /**
     * 更新申报
     */
    DECLARATION_UPDATE("更新申报", "已更新申报"),

    /**
     * 删除申报
     */
    DECLARATION_DELETE("删除申报", "已删除申报"),

    /**
     * 提交申报
     */
    DECLARATION_SUBMIT("提交申报", "已提交申报"),

    /**
     * 审核申报
     */
    DECLARATION_APPROVE("审核申报", "已审核申报"),

    // ==================== 文件管理模块 ====================

    /**
     * 上传文件
     */
    FILE_UPLOAD("上传文件", "已上传文件"),

    /**
     * 删除文件
     */
    FILE_DELETE("删除文件", "已删除文件"),

    /**
     * 下载文件
     */
    FILE_DOWNLOAD("下载文件", "已下载文件"),

    // ==================== 知识库模块 ====================

    /**
     * 创建知识库
     */
    KNOWLEDGE_CREATE("创建知识库", "已创建知识库"),

    /**
     * 更新知识库
     */
    KNOWLEDGE_UPDATE("更新知识库", "已更新知识库"),

    /**
     * 删除知识库
     */
    KNOWLEDGE_DELETE("删除知识库", "已删除知识库"),

    // ==================== 系统配置模块 ====================

    /**
     * 更新系统配置
     */
    SYSTEM_UPDATE_CONFIG("更新系统配置", "已更新系统配置");

    /**
     * 操作名称（用于操作日志的operation字段）
     */
    private final String code;

    /**
     * 详细描述（用于操作日志的detail字段，用户友好格式）
     */
    private final String description;

    OperationLogRecorderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据操作名称获取枚举
     *
     * @param code String 操作名称（如："禁用用户"）
     * @return OperationType 操作类型枚举
     */
    public static OperationLogRecorderStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (OperationLogRecorderStatus type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
