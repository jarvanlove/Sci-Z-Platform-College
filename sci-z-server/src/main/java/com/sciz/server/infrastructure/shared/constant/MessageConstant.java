package com.sciz.server.infrastructure.shared.constant;

/**
 * 消息常量
 *
 * @author JiaWen.Wu
 * @className MessageConstant
 * @date 2025-10-29 10:30
 */
public class MessageConstant {

    /**
     * 成功消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /**
     * 失败消息
     */
    public static final String FAIL_MESSAGE = "操作失败";

    /**
     * 参数错误消息
     */
    public static final String PARAM_ERROR_MESSAGE = "参数错误";

    /**
     * 未授权消息
     */
    public static final String UNAUTHORIZED_MESSAGE = "未授权访问";

    /**
     * 禁止访问消息
     */
    public static final String FORBIDDEN_MESSAGE = "禁止访问";

    /**
     * 资源不存在消息
     */
    public static final String NOT_FOUND_MESSAGE = "资源不存在";

    /**
     * 服务器错误消息
     */
    public static final String SERVER_ERROR_MESSAGE = "服务器内部错误";

    /**
     * 用户相关消息
     */
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String USER_LOGIN_SUCCESS = "登录成功";
    public static final String USER_LOGIN_FAILED = "登录失败";
    public static final String USER_LOGOUT_SUCCESS = "退出成功";
    public static final String USER_REGISTER_SUCCESS = "注册成功";
    public static final String USER_REGISTER_FAILED = "注册失败";
    public static final String USER_PASSWORD_ERROR = "密码错误";
    public static final String USER_ACCOUNT_LOCKED = "账户已锁定";
    public static final String USER_ACCOUNT_DISABLED = "账户已禁用";

    /**
     * 项目相关消息
     */
    public static final String PROJECT_NOT_FOUND = "项目不存在";
    public static final String PROJECT_CREATE_SUCCESS = "项目创建成功";
    public static final String PROJECT_UPDATE_SUCCESS = "项目更新成功";
    public static final String PROJECT_DELETE_SUCCESS = "项目删除成功";
    public static final String PROJECT_STATUS_UPDATE_SUCCESS = "项目状态更新成功";

    /**
     * 申报相关消息
     */
    public static final String DECLARATION_NOT_FOUND = "申报不存在";
    public static final String DECLARATION_SUBMIT_SUCCESS = "申报提交成功";
    public static final String DECLARATION_UPDATE_SUCCESS = "申报更新成功";
    public static final String DECLARATION_DELETE_SUCCESS = "申报删除成功";
    public static final String DECLARATION_APPROVE_SUCCESS = "申报审批成功";
    public static final String DECLARATION_REJECT_SUCCESS = "申报驳回成功";

    /**
     * 报告相关消息
     */
    public static final String REPORT_NOT_FOUND = "报告不存在";
    public static final String REPORT_GENERATE_SUCCESS = "报告生成成功";
    public static final String REPORT_UPDATE_SUCCESS = "报告更新成功";
    public static final String REPORT_DELETE_SUCCESS = "报告删除成功";

    /**
     * 知识库相关消息
     */
    public static final String KNOWLEDGE_NOT_FOUND = "知识库不存在";
    public static final String KNOWLEDGE_CREATE_SUCCESS = "知识库创建成功";
    public static final String KNOWLEDGE_UPDATE_SUCCESS = "知识库更新成功";
    public static final String KNOWLEDGE_DELETE_SUCCESS = "知识库删除成功";
    public static final String KNOWLEDGE_FILE_UPLOAD_SUCCESS = "文件上传成功";
    public static final String KNOWLEDGE_FILE_DELETE_SUCCESS = "文件删除成功";

    /**
     * 对话相关消息
     */
    public static final String CONVERSATION_NOT_FOUND = "对话不存在";
    public static final String CONVERSATION_CREATE_SUCCESS = "对话创建成功";
    public static final String CONVERSATION_UPDATE_SUCCESS = "对话更新成功";
    public static final String CONVERSATION_DELETE_SUCCESS = "对话删除成功";
    public static final String MESSAGE_SEND_SUCCESS = "消息发送成功";

    /**
     * 文件相关消息
     */
    public static final String FILE_UPLOAD_SUCCESS = "文件上传成功";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败";
    public static final String FILE_DOWNLOAD_SUCCESS = "文件下载成功";
    public static final String FILE_DELETE_SUCCESS = "文件删除成功";
    public static final String FILE_NOT_FOUND = "文件不存在";
    public static final String FILE_TYPE_NOT_SUPPORTED = "文件类型不支持";
    public static final String FILE_SIZE_EXCEEDED = "文件大小超出限制";

    /**
     * 验证相关消息
     */
    public static final String VALIDATION_FAILED = "数据验证失败";
    public static final String CAPTCHA_ERROR = "验证码错误";
    public static final String CAPTCHA_EXPIRED = "验证码已过期";
    public static final String TOKEN_EXPIRED = "令牌已过期";
    public static final String TOKEN_INVALID = "令牌无效";
}
