package com.sciz.server.infrastructure.shared.exception;

import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.util.Optional;

/**
 * BusinessException 使用示例
 *
 * 展示如何使用简化的 BusinessException 处理各种业务异常
 *
 * @author JiaWen.Wu
 * @className BusinessExceptionExample
 * @date 2025-10-29 12:00
 */
public class BusinessExceptionExample {

    /**
     * 基础用法示例
     */
    public void basicUsage() {
        // 1. 使用 ResultCode 创建异常（推荐）
        // throw new BusinessException(ResultCode.USER_NOT_FOUND);

        // 2. 使用 ResultCode 和自定义消息（推荐）
        // throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户ID: 123 不存在");

        // 3. 使用自定义错误码和消息
        // throw new BusinessException(400, "请求参数错误");
    }

    /**
     * 常用异常示例
     */
    public void commonExceptions() {
        // 用户相关异常
        // throw new BusinessException(ResultCode.USER_NOT_FOUND);
        // throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        // throw new BusinessException(ResultCode.USER_LOGIN_FAILED);
        // throw new BusinessException(ResultCode.USER_PERMISSION_DENIED);

        // 项目相关异常
        // throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        // throw new BusinessException(ResultCode.PROJECT_ALREADY_EXISTS);
        // throw new BusinessException(ResultCode.PROJECT_PERMISSION_DENIED);

        // 数据验证异常
        // throw new BusinessException(ResultCode.VALIDATION_ERROR);
        // throw new BusinessException(ResultCode.VALIDATION_ERROR, "用户名不能为空");

        // 业务处理异常
        // throw new BusinessException(ResultCode.BUSINESS_ERROR);
        // throw new BusinessException(ResultCode.OPERATION_FAILED);

        // 文件相关异常
        // throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        // throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        // throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
    }

    /**
     * 自定义消息异常示例
     */
    public void customMessageExceptions() {
        // 使用 ResultCode 但自定义消息
        // throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户ID: 123 不存在");
        // throw new BusinessException(ResultCode.PROJECT_NOT_FOUND, "项目名称: '测试项目'
        // 不存在");
        // throw new BusinessException(ResultCode.VALIDATION_ERROR, "用户名长度不能少于3个字符");
    }

    /**
     * 异常处理示例
     */
    public void exceptionHandling() {
        try {
            // 业务逻辑
            processUserRegistration();
        } catch (BusinessException e) {
            // 根据错误码进行不同处理
            if (e.getCode() == ResultCode.VALIDATION_ERROR.getCode()) {
                // 处理验证错误
                handleValidationError(e);
            } else if (e.getCode() == ResultCode.USER_NOT_FOUND.getCode()) {
                // 处理用户不存在错误
                handleNotFoundError(e);
            } else if (e.isClientError()) {
                // 处理客户端错误（4xx）
                handleClientError(e);
            } else if (e.isServerError()) {
                // 处理服务器错误（5xx）
                handleServerError(e);
            }

            // 记录异常信息
            System.err.println("异常详情: " + e.getMessage());
            System.err.println("错误码: " + e.getCode());
            System.err.println("是否为客户端错误: " + e.isClientError());
            System.err.println("是否为服务器错误: " + e.isServerError());
            System.err.println("是否为业务错误: " + e.isBusinessError());
        }
    }

    /**
     * 在应用层服务中的使用示例
     */
    public void serviceLayerUsage() {
        // 用户服务示例
        registerUser("test", "test@example.com");

        // 项目服务示例
        getProject(123L);
    }

    /**
     * 用户注册示例
     */
    public void registerUser(String username, String email) {
        // 1. 参数验证
        var normalizedUsername = Optional.ofNullable(username)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new BusinessException(ResultCode.VALIDATION_ERROR, "用户名不能为空"));
        var normalizedEmail = Optional.ofNullable(email)
                .map(String::trim)
                .filter(value -> value.contains("@"))
                .orElseThrow(() -> new BusinessException(ResultCode.VALIDATION_ERROR, "邮箱格式不正确"));

        // 2. 业务规则验证
        if (userExists(normalizedUsername)) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "用户名已存在");
        }
        if (emailExists(normalizedEmail)) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS, "邮箱已被注册");
        }

        // 3. 业务处理
        try {
            createUser(normalizedUsername, normalizedEmail);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目示例
     */
    public Project getProject(Long projectId) {
        return Optional.ofNullable(projectRepository.findById(projectId))
                .orElseThrow(() -> new BusinessException(ResultCode.PROJECT_NOT_FOUND,
                        "项目ID: " + projectId + " 不存在"));
    }

    // ==================== 私有方法 ====================

    private void processUserRegistration() {
        // 模拟业务逻辑
    }

    private void handleValidationError(BusinessException e) {
        // 处理验证错误
    }

    private void handleNotFoundError(BusinessException e) {
        // 处理资源不存在错误
    }

    private void handleClientError(BusinessException e) {
        // 处理客户端错误
    }

    private void handleServerError(BusinessException e) {
        // 处理服务器错误
    }

    private boolean userExists(String username) {
        // 模拟检查用户是否存在
        return false;
    }

    private boolean emailExists(String email) {
        // 模拟检查邮箱是否存在
        return false;
    }

    private void createUser(String username, String email) {
        // 模拟创建用户
    }

    private ProjectRepository projectRepository = new ProjectRepository();

    private static class ProjectRepository {
        public Project findById(Long id) {
            // 模拟查找项目
            return null;
        }
    }

    // 模拟 Project 类
    private static class Project {
        // 项目实体
    }
}
