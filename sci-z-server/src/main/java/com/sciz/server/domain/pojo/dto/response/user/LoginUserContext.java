package com.sciz.server.domain.pojo.dto.response.user;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录用户上下文
 *
 * <p>
 * 缓存在会话中的当前登录用户基础信息，方便后端业务读取。
 * </p>
 *
 * @param userId         Long 用户ID
 * @param username       String 用户名
 * @param realName       String 真实姓名
 * @param industryType   String 行业类型
 * @param departmentId   Long 部门ID
 * @param departmentName String 部门名称
 * @param email          String 邮箱
 * @param phone          String 手机号
 *
 * @author JiaWen.Wu
 * @className LoginUserContext
 * @date 2025-11-11 15:20
 */
public record LoginUserContext(
        Long userId,
        String username,
        String realName,
        String industryType,
        Long departmentId,
        String departmentName,
        String email,
        String phone) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构建登录用户上下文
     *
     * @param userId         Long 用户ID
     * @param username       String 用户名
     * @param realName       String 真实姓名
     * @param industryType   String 行业类型
     * @param departmentId   Long 部门ID
     * @param departmentName String 部门名称
     * @param email          String 邮箱
     * @param phone          String 手机号
     * @return LoginUserContext 上下文对象
     */
    public static LoginUserContext of(Long userId,
            String username,
            String realName,
            String industryType,
            Long departmentId,
            String departmentName,
            String email,
            String phone) {
        return new LoginUserContext(userId, username, realName, industryType, departmentId, departmentName, email,
                phone);
    }
}
