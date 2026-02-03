package com.sciz.server.application.service.file;

import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.file.FileCheckDuplicateReq;
import com.sciz.server.domain.pojo.dto.request.file.FileListQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileDownloadContext;
import com.sciz.server.domain.pojo.dto.response.file.FileDuplicateCheckResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.file.FileUploadResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import java.util.List;

/**
 * 文件应用服务
 *
 * @author JiaWen.Wu
 * @className FileService
 * @date 2025-10-30 13:35
 */
public interface FileService {

    /**
     * 单文件上传
     *
     * @param req FileUploadReq 上传请求
     * @return FileInfoResp 文件信息
     */
    FileInfoResp upload(FileUploadReq req);

    /**
     * 单文件上传（支持异步上下文）
     * <p>
     * 用于异步任务等非 Web 上下文场景，需要手动传入用户信息
     *
     * @param req      FileUploadReq 上传请求
     * @param userId   Long 上传人ID
     * @param realName String 上传人姓名
     * @return FileInfoResp 文件信息
     */
    FileInfoResp upload(FileUploadReq req, Long userId, String realName);

    /**
     * 批量上传
     *
     * @param req FileBatchUploadReq 上传请求
     * @return List<FileInfoResp> 上传结果
     */
    List<FileInfoResp> uploadBatch(FileBatchUploadReq req);

    /**
     * 批量上传（支持进度返回，支持部分成功）
     * <p>
     * 与 uploadBatch 的区别：
     * 1. 提前校验文件类型和大小，过滤不支持的文件
     * 2. 支持部分成功，即使部分文件失败也返回结果
     * 3. 返回详细的进度信息，包含每个文件的上传状态和阶段
     *
     * @param req FileBatchUploadReq 上传请求
     * @return List<FileUploadResp> 每个文件的上传结果列表（包含成功和失败的详细信息）
     */
    List<FileUploadResp> uploadBatchWithProgress(FileBatchUploadReq req);

    /**
     * 文件分页列表
     *
     * @param req FileListQueryReq 查询请求
     * @return PageResult<FileInfoResp> 分页结果
     */
    PageResult<FileInfoResp> page(FileListQueryReq req);

    /**
     * 文件下载
     *
     * @param attachmentId Long 附件ID
     * @return FileDownloadContext 下载上下文
     */
    FileDownloadContext download(Long attachmentId);

    /**
     * 文件下载（支持格式转换）
     *
     * @param attachmentId Long 附件ID
     * @param targetFormat String 目标格式（docx/pdf，null表示不转换）
     * @return FileDownloadContext 下载上下文
     */
    FileDownloadContext download(Long attachmentId, String targetFormat);

    /**
     * 预览地址
     *
     * @param attachmentId  Long 附件ID
     * @param expireSeconds Integer 预签名过期秒数
     * @return String 预览URL
     */
    String preview(Long attachmentId, Integer expireSeconds);

    /**
     * 根据file_url生成预签名URL
     * file_url格式：bucketName/filePath（如：sciz-files/2025/11/19/xxx.jpeg）
     *
     * @param fileUrl       String 文件URL（格式：bucketName/filePath）
     * @param expireSeconds Integer 预签名过期秒数（可选，默认使用系统配置）
     * @return String 预签名URL
     */
    String generatePresignedUrlFromFileUrl(String fileUrl, Integer expireSeconds);

    /**
     * 删除附件
     *
     * @param attachmentId Long 附件ID
     */
    void delete(Long attachmentId);

    /**
     * 检查文件是否重复
     *
     * @param req FileCheckDuplicateReq 请求
     * @return FileDuplicateCheckResp 去重结果
     */
    FileDuplicateCheckResp checkDuplicate(FileCheckDuplicateReq req);
}