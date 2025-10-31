package com.sciz.server.domain.pojo.dto.response.user;

import lombok.Data;

/**
 * 登录返回中的用户信息
 *
 * @author JiaWen.Wu
 * @className LoginUserInfoResp
 * @date 2025-10-31 11:20
 */
@Data
public class LoginUserInfoResp {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 真实姓名 */
    private String realName;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 部门/院系/科室 */
    private String department;
    /** 角色编码（主角色） */
    private String role;
    /** 状态：active/disabled 等 */
    private String status;
    /** 行业类型：education/medical/power */
    private String industry;
    /** 头像URL */
    private String avatar;
}
