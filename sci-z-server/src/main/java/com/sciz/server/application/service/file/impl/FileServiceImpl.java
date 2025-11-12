package com.sciz.server.application.service.file.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.file.FileService;
import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.file.FileCheckDuplicateReq;
import com.sciz.server.domain.pojo.dto.request.file.FileListQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileDownloadContext;
import com.sciz.server.domain.pojo.dto.response.file.FileDuplicateCheckResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import com.sciz.server.domain.pojo.entity.file.SysAttachmentRelation;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRelationRepo;
import com.sciz.server.domain.pojo.repository.file.SysAttachmentRepo;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.file.FileDeletedEvent;
import com.sciz.server.infrastructure.shared.event.file.FileUploadedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.FileUtil;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.shared.utils.MinioUtil;
import com.sciz.server.interfaces.converter.FileConverter;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * 文件应用服务实现类
 *
 * @author JiaWen.Wu
 * @className FileService
 * @date 2025-10-30 13:35
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final int DEFAULT_PREVIEW_EXPIRE_SECONDS = 600;
    private static final DateTimeFormatter DATE_FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(SystemConstant.DATE_TIME_FORMAT);

    private final MinioClient minioClient;
    private final SysAttachmentRepo sysAttachmentRepo;
    private final SysAttachmentRelationRepo sysAttachmentRelationRepo;
    private final FileConverter fileConverter;
    private final EventPublisher eventPublisher;

    @Value("${minio.bucket:sciz-files}")
    private String bucketName;

    private final AtomicBoolean bucketInitialized = new AtomicBoolean(false);

    /**
     * 单文件上传
     *
     * @param req FileUploadReq 上传请求
     * @return FileInfoResp 文件信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoResp upload(FileUploadReq req) {
        validateUploadRequest(req);
        var currentUser = LoginUserUtil.requireCurrentUser();
        ensureBucket();

        var attachment = buildAttachment(req, currentUser.userId(), currentUser.realName());
        uploadToMinio(req.getFile(), attachment.getFilePath(), attachment.getMimeType(), attachment.getFileSize());

        Long attachmentId = sysAttachmentRepo.save(attachment);
        if (attachmentId == null) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "附件保存失败");
        }

        saveRelationIfNecessary(req, attachmentId);
        publishUploadEvent(req, currentUser.userId(), currentUser.realName(), attachment);

        return buildFileInfoResp(attachment, DEFAULT_PREVIEW_EXPIRE_SECONDS);
    }

    /**
     * 批量文件上传
     *
     * @param req FileBatchUploadReq 上传请求
     * @return List<FileInfoResp> 上传结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileInfoResp> uploadBatch(FileBatchUploadReq req) {
        if (req == null || req.files() == null || req.files().length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件列表不能为空");
        }
        List<FileInfoResp> responses = new ArrayList<>();
        for (var multipartFile : req.files()) {
            FileUploadReq singleReq = new FileUploadReq();
            singleReq.setFile(multipartFile);
            singleReq.setRelationType(req.relationType());
            singleReq.setRelationId(req.relationId());
            singleReq.setRelationName(req.relationName());
            singleReq.setAttachmentType(req.attachmentType());
            singleReq.setIsPublic(req.isPublic());
            responses.add(upload(singleReq));
        }
        return responses;
    }

    /**
     * 文件分页列表
     *
     * @param req FileListQueryReq 查询请求
     * @return PageResult<FileInfoResp> 分页结果
     */
    @Override
    public PageResult<FileInfoResp> page(FileListQueryReq req) {
        BaseQueryReq baseQuery = req.toBaseQuery();
        Page<SysAttachment> page = new Page<>(baseQuery.pageNo(), baseQuery.pageSize());

        List<Long> relationAttachmentIds = sysAttachmentRelationRepo.findAttachmentIds(req.relationType(),
                req.relationId());

        boolean asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        String sortColumn = normalizeSortColumn(baseQuery.sortBy());

        IPage<SysAttachment> attachmentPage = sysAttachmentRepo.page(page,
                req.relationType(),
                relationAttachmentIds,
                req.attachmentType(),
                req.isPublic(),
                req.uploaderId(),
                req.keyword(),
                sortColumn,
                asc);

        List<FileInfoResp> records = attachmentPage.getRecords().stream()
                .map(item -> buildFileInfoResp(item, DEFAULT_PREVIEW_EXPIRE_SECONDS))
                .toList();

        Page<FileInfoResp> resultPage = new Page<>(attachmentPage.getCurrent(), attachmentPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(attachmentPage.getTotal());
        return PageResult.of(resultPage);
    }

    /**
     * 文件下载
     *
     * @param attachmentId Long 附件ID
     * @return FileDownloadContext 下载上下文
     */
    @Override
    public FileDownloadContext download(Long attachmentId) {
        SysAttachment attachment = obtainAttachment(attachmentId);
        ensureBucket();
        try {
            GetObjectResponse response = MinioUtil.download(minioClient, bucketName, attachment.getFilePath());
            sysAttachmentRepo.increaseDownloadCount(attachmentId,
                    LoginUserUtil.getCurrentUserId().orElse(null));
            return new FileDownloadContext(attachment.getFileName(),
                    attachment.getOriginalName(),
                    attachment.getMimeType(),
                    attachment.getFileSize(),
                    response);
        } catch (Exception exception) {
            log.error(String.format("文件下载失败: attachmentId=%s", attachmentId), exception);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "文件下载失败");
        }
    }

    /**
     * 生成预览链接
     *
     * @param attachmentId  Long 附件ID
     * @param expireSeconds Integer 过期秒数
     * @return String 预览URL
     */
    @Override
    public String preview(Long attachmentId, Integer expireSeconds) {
        SysAttachment attachment = obtainAttachment(attachmentId);
        int effectiveExpire = (expireSeconds == null || expireSeconds <= 0)
                ? DEFAULT_PREVIEW_EXPIRE_SECONDS
                : expireSeconds;
        ensureBucket();
        try {
            return MinioUtil.presignedGetUrl(minioClient, bucketName, attachment.getFilePath(), effectiveExpire);
        } catch (Exception exception) {
            log.error(String.format("获取文件预览链接失败: attachmentId=%s", attachmentId), exception);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "获取文件预览链接失败");
        }
    }

    /**
     * 删除文件
     *
     * @param attachmentId Long 附件ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long attachmentId) {
        SysAttachment attachment = obtainAttachment(attachmentId);
        ensureBucket();
        try {
            MinioUtil.deleteObject(minioClient, bucketName, attachment.getFilePath());
        } catch (Exception exception) {
            log.error(String.format("删除 MinIO 对象失败: attachmentId=%s", attachmentId), exception);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "删除存储对象失败");
        }

        Long operatorId = LoginUserUtil.getCurrentUserId().orElse(null);
        if (!sysAttachmentRepo.markDeleted(attachmentId, operatorId)) {
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "删除数据库记录失败");
        }
        sysAttachmentRelationRepo.deleteByAttachmentId(attachmentId);
        publishDeleteEvent(attachment, operatorId);
    }

    /**
     * 文件去重
     *
     * @param req FileCheckDuplicateReq 请求
     * @return FileDuplicateCheckResp 去重结果
     */
    @Override
    public FileDuplicateCheckResp checkDuplicate(FileCheckDuplicateReq req) {
        if (req == null || !StringUtils.hasText(req.md5())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件MD5不能为空");
        }
        SysAttachment attachment = sysAttachmentRepo.findByMd5(req.md5());
        if (attachment == null) {
            return new FileDuplicateCheckResp(false, null);
        }
        FileInfoResp infoResp = buildFileInfoResp(attachment, DEFAULT_PREVIEW_EXPIRE_SECONDS);
        return new FileDuplicateCheckResp(true, infoResp);
    }

    /**
     * 校验上传请求
     */
    private void validateUploadRequest(FileUploadReq req) {
        if (req == null || req.getFile() == null || req.getFile().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (FileUtil.isFileSizeExceeded(req.getFile().getSize())) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }
        if (!FileUtil.isSupportedFileType(req.getFile().getOriginalFilename())) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * 构建附件实体
     */
    private SysAttachment buildAttachment(FileUploadReq req, Long userId, String realName) {
        var multipartFile = req.getFile();
        String originalName = StringUtils.hasText(multipartFile.getOriginalFilename())
                ? multipartFile.getOriginalFilename()
                : multipartFile.getName();
        String extension = FileUtil.getFileExtension(originalName);
        String objectName = buildObjectName(originalName);
        String mimeType = resolveMimeType(multipartFile, originalName);
        String md5 = computeMd5(multipartFile);

        var now = LocalDateTime.now();
        SysAttachment attachment = new SysAttachment();
        attachment.setFileName(objectName);
        attachment.setOriginalName(originalName);
        attachment.setFileType(StringUtils.hasText(req.getAttachmentType()) ? req.getAttachmentType() : extension);
        attachment.setFileExtension(extension);
        attachment.setFileSize(multipartFile.getSize());
        attachment.setFileUrl(String.format("%s/%s", bucketName, objectName));
        attachment.setFilePath(objectName);
        attachment.setMimeType(mimeType);
        attachment.setMd5Hash(md5);
        attachment.setUploaderId(userId);
        attachment.setUploaderName(realName);
        attachment.setUploadTime(now);
        attachment.setDownloadCount(0);
        attachment.setIsPublic(Objects.requireNonNullElse(req.getIsPublic(), 0));
        attachment.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        attachment.setCreatedBy(userId);
        attachment.setUpdatedBy(userId);
        attachment.setCreatedTime(now);
        attachment.setUpdatedTime(now);
        return attachment;
    }

    /**
     * 上传至 MinIO
     */
    private void uploadToMinio(InputStreamProvider fileProvider, String objectName, String mimeType, long size) {
        try (InputStream inputStream = fileProvider.provide()) {
            MinioUtil.upload(minioClient, bucketName, objectName, inputStream, size, mimeType);
        } catch (Exception exception) {
            log.error(String.format("上传文件到 MinIO 失败: objectName=%s", objectName), exception);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "上传文件到存储失败");
        }
    }

    private void uploadToMinio(org.springframework.web.multipart.MultipartFile multipartFile,
            String objectName,
            String mimeType,
            long size) {
        uploadToMinio(multipartFile::getInputStream, objectName, mimeType, size);
    }

    /**
     * 保存附件关联
     */
    private void saveRelationIfNecessary(FileUploadReq req, Long attachmentId) {
        if (!StringUtils.hasText(req.getRelationType()) || req.getRelationId() == null) {
            return;
        }
        SysAttachmentRelation relation = new SysAttachmentRelation();
        relation.setAttachmentId(attachmentId);
        relation.setRelationType(req.getRelationType());
        relation.setRelationId(req.getRelationId());
        relation.setRelationName(req.getRelationName());
        relation.setAttachmentType(req.getAttachmentType());
        relation.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        relation.setCreatedTime(LocalDateTime.now());
        relation.setUpdatedTime(LocalDateTime.now());
        sysAttachmentRelationRepo.save(relation);
    }

    /**
     * 发布上传事件
     */
    private void publishUploadEvent(FileUploadReq req, Long userId, String realName, SysAttachment attachment) {
        try {
            FileUploadedEvent event = new FileUploadedEvent(String.valueOf(attachment.getId()),
                    attachment.getOriginalName(),
                    attachment.getFileType(),
                    String.valueOf(attachment.getFileSize()),
                    attachment.getFilePath(),
                    bucketName,
                    String.valueOf(userId),
                    realName,
                    req.getRelationId() != null ? String.valueOf(req.getRelationId()) : null,
                    req.getRelationName(),
                    attachment.getUploadTime().format(DATE_TIME_FORMATTER),
                    attachment.getMd5Hash());
            eventPublisher.publishAsync(event);
        } catch (Exception exception) {
            log.warn(String.format("发布文件上传事件失败: attachmentId=%s", attachment.getId()), exception);
        }
    }

    /**
     * 发布删除事件
     */
    private void publishDeleteEvent(SysAttachment attachment, Long operatorId) {
        try {
            FileDeletedEvent event = new FileDeletedEvent(String.valueOf(attachment.getId()),
                    attachment.getOriginalName(),
                    attachment.getFileType(),
                    attachment.getFilePath(),
                    bucketName,
                    operatorId != null ? String.valueOf(operatorId) : null,
                    attachment.getUploaderName(),
                    null,
                    null,
                    LocalDateTime.now().format(DATE_TIME_FORMATTER),
                    "USER_DELETE");
            eventPublisher.publishAsync(event);
        } catch (Exception exception) {
            log.warn(String.format("发布文件删除事件失败: attachmentId=%s", attachment.getId()), exception);
        }
    }

    /**
     * 构建响应
     */
    private FileInfoResp buildFileInfoResp(SysAttachment attachment, int expireSeconds) {
        FileInfoResp resp = fileConverter.toInfoResp(attachment);
        if (resp == null) {
            return null;
        }
        String previewUrl = generatePreviewUrlSafely(attachment.getFilePath(), expireSeconds);
        return resp.withBucketName(bucketName).withPreviewUrl(previewUrl);
    }

    /**
     * 安全生成预览链接
     */
    private String generatePreviewUrlSafely(String objectName, int expireSeconds) {
        try {
            ensureBucket();
            return MinioUtil.presignedGetUrl(minioClient, bucketName, objectName, expireSeconds);
        } catch (Exception exception) {
            log.warn(String.format("生成预览链接失败: objectName=%s", objectName), exception);
            return null;
        }
    }

    /**
     * 获取附件
     */
    private SysAttachment obtainAttachment(Long attachmentId) {
        SysAttachment attachment = sysAttachmentRepo.findById(attachmentId);
        if (attachment == null || DeleteStatus.DELETED.getCode().equals(attachment.getIsDeleted())) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    /**
     * 确保桶存在
     */
    private void ensureBucket() {
        if (bucketInitialized.compareAndSet(false, true)) {
            try {
                MinioUtil.makeBucketIfAbsent(minioClient, bucketName);
            } catch (Exception exception) {
                log.error(String.format("初始化 MinIO 桶失败: bucket=%s", bucketName), exception);
                bucketInitialized.set(false);
                throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "初始化存储桶失败");
            }
        }
    }

    /**
     * 构建对象名
     */
    private String buildObjectName(String originalName) {
        String folder = LocalDate.now().format(DATE_FOLDER_FORMAT);
        String uniqueName = FileUtil.generateUniqueFileName(originalName);
        return String.format("%s/%s", folder, uniqueName);
    }

    /**
     * 解析 MIME 类型
     */
    private String resolveMimeType(org.springframework.web.multipart.MultipartFile file, String originalName) {
        String mimeType = file.getContentType();
        if (!StringUtils.hasText(mimeType)) {
            mimeType = FileUtil.getMimeType(originalName);
        }
        return StringUtils.hasText(mimeType) ? mimeType : "application/octet-stream";
    }

    /**
     * 计算 MD5
     */
    private String computeMd5(org.springframework.web.multipart.MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException exception) {
            log.error("计算文件MD5失败", exception);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "计算文件MD5失败");
        }
    }

    /**
     * 标准化排序字段
     */
    private String normalizeSortColumn(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "uploadTime";
        }
        return switch (sortBy) {
            case "fileSize" -> "fileSize";
            case "downloadCount" -> "downloadCount";
            case "uploadTime" -> "uploadTime";
            default -> "uploadTime";
        };
    }

    /**
     * 输入流提供者
     */
    @FunctionalInterface
    private interface InputStreamProvider {
        InputStream provide() throws IOException;
    }
}
