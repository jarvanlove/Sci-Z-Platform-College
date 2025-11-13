package com.sciz.server.application.service.file;

import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.file.FileCheckDuplicateReq;
import com.sciz.server.domain.pojo.dto.request.file.FileListQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileDownloadContext;
import com.sciz.server.domain.pojo.dto.response.file.FileDuplicateCheckResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
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
     * 批量上传
     *
     * @param req FileBatchUploadReq 上传请求
     * @return List<FileInfoResp> 上传结果
     */
    List<FileInfoResp> uploadBatch(FileBatchUploadReq req);

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
     * 预览地址
     *
     * @param attachmentId  Long 附件ID
     * @param expireSeconds Integer 预签名过期秒数
     * @return String 预览URL
     */
    String preview(Long attachmentId, Integer expireSeconds);

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