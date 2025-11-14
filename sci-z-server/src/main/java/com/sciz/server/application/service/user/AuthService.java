package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.user.EmailCodeSendReq;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.request.user.PhoneCodeSendReq;
import com.sciz.server.domain.pojo.dto.request.user.RegisterReq;
import com.sciz.server.domain.pojo.dto.request.user.ChangePasswordReq;
import com.sciz.server.domain.pojo.dto.request.user.ResetPasswordReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckLoginResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckRoleResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckPermResp;
import com.sciz.server.domain.pojo.dto.response.user.RefreshTokenResp;
import com.sciz.server.domain.pojo.dto.response.user.RegisterResp;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.request.user.UserInfoUpdateReq;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.user.UserInfoUpdateResp;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证应用服务
 * 
 * @author JiaWen.Wu
 * @className AuthService
 * @date 2025-10-28 00:00
 */
public interface AuthService {
    /**
     * 用户登录
     *
     * @param loginReq LoginReq 登录请求（包含用户名、密码、验证码等）
     * @param request  HttpServletRequest 请求对象（用于获取客户端信息）
     * @return LoginResp 登录结果
     */
    LoginResp login(LoginReq loginReq, HttpServletRequest request);

    /**
     * 用户登出
     * 注销当前登录会话，清理 token 和权限缓存
     */
    void logout();

    /**
     * 获取验证码
     * 生成图形验证码并缓存到 Redis
     *
     * @return CaptchaResp 验证码响应（包含 key 和 Base64 图片）
     */
    CaptchaResp getCaptcha();

    /**
     * 用户注册
     *
     * @param registerReq RegisterReq 注册请求
     * @return RegisterResp 注册结果
     */
    RegisterResp register(RegisterReq registerReq);

    /**
     * 查询当前登录用户档案
     *
     * @return ProfileResp 用户档案
     */
    ProfileResp profile();

    /**
     * 刷新或续期当前会话 Token
     *
     * @return RefreshTokenResp 刷新结果
     */
    RefreshTokenResp refreshToken();

    /**
     * 校验当前是否登录
     *
     * @return CheckLoginResp 登录校验结果
     */
    CheckLoginResp checkLogin();

    /**
     * 校验当前登录用户是否拥有指定角色
     *
     * @param roleCode String 角色编码
     * @return CheckRoleResp 角色校验结果
     */
    CheckRoleResp checkRole(String roleCode);

    /**
     * 校验当前登录用户是否拥有指定权限
     *
     * @param permissionCode String 权限编码
     * @return CheckPermResp 权限校验结果
     */
    CheckPermResp checkPermission(String permissionCode);

    /**
     * 发送邮箱验证码
     *
     * @param req EmailCodeSendReq 请求参数
     */
    void sendEmailVerificationCode(EmailCodeSendReq req);

    /**
     * 发送短信验证码
     *
     * @param req PhoneCodeSendReq 请求参数
     */
    void sendSmsVerificationCode(PhoneCodeSendReq req);

    /**
     * 重置密码
     *
     * @param req ResetPasswordReq 重置密码请求
     */
    void resetPassword(ResetPasswordReq req);

    /**
     * 修改密码
     * 已登录用户修改密码，需要验证原密码
     *
     * @param req ChangePasswordReq 修改密码请求
     */
    void changePassword(ChangePasswordReq req);

    /**
     * 更新个人信息
     * 仅更新基础信息（真实姓名、邮箱、手机号、部门、职称），不包含头像
     *
     * @param req UserInfoUpdateReq 个人信息更新请求
     * @return UserInfoUpdateResp 更新后的个人信息
     */
    UserInfoUpdateResp updateUserInfo(UserInfoUpdateReq req);

    /**
     * 上传用户头像
     * 调用文件上传服务处理文件上传，然后更新用户头像URL和附件ID
     * 入参和出参与 api/file/upload 保持一致
     *
     * @param req FileUploadReq 文件上传请求（
     * @return FileInfoResp 文件信息响应
     */
    FileInfoResp uploadAvatar(FileUploadReq req);
}
