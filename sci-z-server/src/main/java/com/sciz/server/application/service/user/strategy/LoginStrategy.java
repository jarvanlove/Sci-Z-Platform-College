package com.sciz.server.application.service.user.strategy;

import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.entity.user.SysUser;

/**
 * 登录策略接口
 * 使用策略模式支持多种登录方式（密码登录、短信登录、扫码登录、微信登录等）
 *
 * @author JiaWen.Wu
 * @className LoginStrategy
 * @date 2025-01-15 17:30
 */
public interface LoginStrategy {

    /**
     * 获取登录类型
     *
     * @return 登录类型代码
     */
    String getLoginType();

    /**
     * 校验登录参数
     *
     * @param loginReq 登录请求
     */
    void validateParams(LoginReq loginReq);

    /**
     * 查找用户
     * 根据登录方式的不同，通过不同的字段查找用户（如用户名、手机号等）
     *
     * @param loginReq 登录请求
     * @return 用户实体
     */
    SysUser findUser(LoginReq loginReq);

    /**
     * 校验登录凭证
     * 根据登录方式的不同，校验不同的凭证（如密码、短信验证码等）
     *
     * @param loginReq 登录请求
     * @param user     用户实体
     */
    void validateCredential(LoginReq loginReq, SysUser user);

    /**
     * 获取用于失败计数的标识
     * 用于登录失败时记录失败次数（如用户名、手机号等）
     *
     * @param loginReq 登录请求
     * @return 标识字符串
     */
    String getFailCountKey(LoginReq loginReq);
}

