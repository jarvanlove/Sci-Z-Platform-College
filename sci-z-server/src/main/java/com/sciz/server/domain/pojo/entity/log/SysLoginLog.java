package com.sciz.server.domain.pojo.entity.log;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录日志实体
 *
 * - 对应表：对应表：sys_login_log
 * - 记录用户登录信息
 *
 * @author JiaWen.Wu
 * @className SysLoginLog
 * @date 2025-10-29 16:00
 */
@Getter
@Setter
@TableName("sys_login_log")
public class SysLoginLog extends BaseEntity {

    /**
     * 日志ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联sys_user表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录IP地址
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录地点
     */
    @TableField("login_location")
    private String loginLocation;

    /**
     * 浏览器信息
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统信息
     */
    @TableField("os")
    private String os;

    /**
     * 登录状态：1=成功，0=失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 提示消息
     */
    @TableField("message")
    private String message;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
}
