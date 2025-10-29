package com.sciz.server.domain.pojo.entity.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统操作日志实体
 * 对应表：sys_operation_log
 *
 * 字段涵盖：用户信息、操作信息、请求/响应、客户端信息、状态与耗时
 *
 * @author JiaWen.Wu
 * @className SysOperationLog
 * @date 2025-10-29 16:00
 */
@Getter
@Setter
@TableName("sys_operation_log")
public class SysOperationLog extends BaseEntity {

    /**
     * 日志ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 操作类型
     */
    @TableField("operation")
    private String operation;

    /**
     * 请求方法（GET/POST等）
     */
    @TableField("method")
    private String method;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求参数（已脱敏/截断）
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应结果（已脱敏/截断）
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 操作地点
     */
    @TableField("location")
    private String location;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 操作状态（0:失败,1:成功）
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 执行时长（毫秒）
     */
    @TableField("execution_time")
    private Integer executionTime;
}
