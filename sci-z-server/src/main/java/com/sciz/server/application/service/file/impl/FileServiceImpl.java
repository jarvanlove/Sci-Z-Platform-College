package com.sciz.server.application.service.file.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.file.FileService;
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
import com.sciz.server.infrastructure.shared.enums.AttachmentCategoryStatus;
import com.sciz.server.infrastructure.shared.enums.AttachmentRelationStatus;
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
import org.springframework.web.multipart.MultipartFile;

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
        // 1. 参数校验
        validateUploadRequest(req);
        // 2. 解析附件分类并校验关联信息
        var category = normalizeAttachmentCategory(req.getAttachmentType());
        req.setAttachmentType(category.getCode());
        validateRelationParams(req);
        // 3. 获取当前用户
        var currentUser = LoginUserUtil.requireCurrentUser();
        log.info(String.format("文件上传开始: originalName=%s, uploaderId=%s",
                req.getFile().getOriginalFilename(), currentUser.userId()));
        // 4. 确保存储桶可用
        ensureBucket();
        // 5. 构建附件实体
        var attachment = buildAttachment(req, currentUser.userId(), currentUser.realName(), category);
        // 6. 上传至对象存储
        uploadToObjectStorage(req, attachment);
        // 7. 持久化附件记录
        var attachmentId = persistAttachment(attachment);
        // 8. 记录业务关联（可选）
        saveRelationIfNecessary(req, attachmentId);

        // 9. 发布上传事件
        publishUploadEvent(req, currentUser.userId(), currentUser.realName(), attachment);

        // 10. 构建响应并返回
        FileInfoResp resp = buildFileInfoResp(attachment, DEFAULT_PREVIEW_EXPIRE_SECONDS);
        log.info(String.format("文件上传完成: attachmentId=%s, uploaderId=%s",
                attachment.getId(), currentUser.userId()));
        return resp;
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
        // 1. 校验批量上传请求
        validateBatchUploadRequest(req);

        // 2. 遍历上传每一个文件
        log.info(String.format("批量上传文件: fileCount=%s, relationType=%s", req.files().length, req.relationType()));
        var responses = new ArrayList<FileInfoResp>(req.files().length);
        for (var multipartFile : req.files()) {
            var singleReq = buildSingleUploadReq(req, multipartFile);
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
        var baseQuery = req.toBaseQuery();
        var page = new Page<SysAttachment>(baseQuery.pageNo(), baseQuery.pageSize());

        var relationAttachmentIds = sysAttachmentRelationRepo.findAttachmentIds(req.relationType(), req.relationId());

        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortColumn = normalizeSortColumn(baseQuery.sortBy());

        IPage<SysAttachment> attachmentPage = sysAttachmentRepo.page(page,
                req.relationType(),
                relationAttachmentIds,
                req.attachmentType(),
                req.isPublic(),
                req.uploaderId(),
                req.keyword(),
                sortColumn,
                asc);

        var records = attachmentPage.getRecords().stream()
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
        // 1. 查询附件
        var attachment = obtainAttachment(attachmentId);

        // 2. 确保存储桶可用
        ensureBucket();

        // 3. 执行下载并更新统计
        return downloadFromObjectStorage(attachmentId, attachment);
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
        // 1. 查询附件
        var attachment = obtainAttachment(attachmentId);

        // 2. 计算有效期
        var effectiveExpire = (expireSeconds == null || expireSeconds <= 0)
                ? DEFAULT_PREVIEW_EXPIRE_SECONDS
                : expireSeconds;

        // 3. 生成预签名链接
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
        // 1. 查询附件
        var attachment = obtainAttachment(attachmentId);

        // 2. 删除对象存储文件
        removeObjectFromStorage(attachmentId, attachment);

        // 3. 删除数据库记录与关联
        var operatorId = LoginUserUtil.getCurrentUserId().orElse(null);
        if (!sysAttachmentRepo.markDeleted(attachmentId, operatorId)) {
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "删除数据库记录失败");
        }
        sysAttachmentRelationRepo.deleteByAttachmentId(attachmentId);

        // 4. 发布删除事件
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
        var attachment = sysAttachmentRepo.findByMd5(req.md5());
        if (attachment == null) {
            return new FileDuplicateCheckResp(false, null);
        }
        var infoResp = buildFileInfoResp(attachment, DEFAULT_PREVIEW_EXPIRE_SECONDS);
        return new FileDuplicateCheckResp(true, infoResp);
    }

    /**
     * 校验上传请求
     *
     * @param req FileUploadReq 上传请求
     */
    private void validateUploadRequest(FileUploadReq req) {
        if (req == null || req.getFile() == null || req.getFile().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (FileUtil.isFileSizeExceeded(req.getFile().getSize())) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }
        if (!FileUtil.isSupportedFileType(req.getFile().getOriginalFilename())
                && !FileUtil.isSupportedMimeType(req.getFile().getContentType())) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * 解析并校验附件分类
     *
     * @param attachmentType String 附件分类编码
     * @return AttachmentCategoryStatus 分类枚举
     */
    private AttachmentCategoryStatus normalizeAttachmentCategory(String attachmentType) {
        if (!StringUtils.hasText(attachmentType)) {
            return AttachmentCategoryStatus.defaultType();
        }
        try {
            return AttachmentCategoryStatus.fromCode(attachmentType);
        } catch (IllegalArgumentException exception) {
            log.warn(String.format("不支持的附件类型: attachmentType=%s", attachmentType));
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的附件类型");
        }
    }

    /**
     * 校验附件关联参数
     *
     * @param req FileUploadReq 上传请求
     */
    private void validateRelationParams(FileUploadReq req) {
        var hasRelationType = StringUtils.hasText(req.getRelationType());
        if (!hasRelationType) {
            if (req.getRelationId() != null || StringUtils.hasText(req.getRelationName())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "未指定关联类型时不能提供关联对象信息");
            }
            return;
        }

        try {
            AttachmentRelationStatus.fromCode(req.getRelationType());
        } catch (IllegalArgumentException exception) {
            log.warn(String.format("不支持的附件关联类型: relationType=%s", req.getRelationType()));
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的附件关联类型");
        }

        if (req.getRelationId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "附件关联对象ID不能为空");
        }
    }

    /**
     * 校验批量上传请求
     *
     * @param req FileBatchUploadReq 批量上传请求
     */
    private void validateBatchUploadRequest(FileBatchUploadReq req) {
        if (req == null || req.files() == null || req.files().length == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件列表不能为空");
        }
    }

    /**
     * 构建附件实体
     */
    private SysAttachment buildAttachment(FileUploadReq req, Long userId, String realName,
            AttachmentCategoryStatus category) {
        var multipartFile = req.getFile();
        var originalName = resolveOriginalName(multipartFile);
        var extension = FileUtil.getFileExtension(originalName);
        var objectName = buildObjectName(originalName);
        var mimeType = resolveMimeType(multipartFile, originalName);
        var md5 = computeMd5(multipartFile);
        var now = LocalDateTime.now();

        var attachment = new SysAttachment();
        attachment.setFileName(objectName);
        attachment.setOriginalName(originalName);
        attachment.setFileType(category.getCode());
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

    /**
     * 上传至 MinIO
     */
    private void uploadToMinio(MultipartFile multipartFile,
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
        var relation = new SysAttachmentRelation();
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

    /*
     * 发布上传事件
     *
     * @param req FileUploadReq 上传请求
     * 
     * @param userId Long 上传人 ID
     * 
     * @param realName String 上传人姓名
     * 
     * @param attachment SysAttachment 附件实体
     */

    /**
     * 发布上传事件
     */
    private void publishUploadEvent(FileUploadReq req, Long userId, String realName, SysAttachment attachment) {
        try {
            var event = new FileUploadedEvent(String.valueOf(attachment.getId()),
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
            var event = new FileDeletedEvent(String.valueOf(attachment.getId()),
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
        var resp = fileConverter.toInfoResp(attachment);
        if (resp == null) {
            return null;
        }
        var previewUrl = generatePreviewUrlSafely(attachment.getFilePath(), expireSeconds);
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
        if (!bucketInitialized.compareAndSet(false, true)) {
            return;
        }
        try {
            MinioUtil.makeBucketIfAbsent(minioClient, bucketName);
        } catch (Exception exception) {
            bucketInitialized.set(false);
            log.error(String.format("初始化 MinIO 桶失败: bucket=%s", bucketName), exception);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED,
                    String.format("初始化存储桶失败: %s", exception.getMessage()));
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
    private String resolveMimeType(MultipartFile file, String originalName) {
        var mimeType = file.getContentType();
        if (!StringUtils.hasText(mimeType)) {
            mimeType = FileUtil.getMimeType(originalName);
        }
        return StringUtils.hasText(mimeType) ? mimeType : "application/octet-stream";
    }

    /**
     * 计算 MD5
     */
    private String computeMd5(MultipartFile file) {
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
     * 构建单文件上传请求（供批量上传复用）
     *
     * @param req           FileBatchUploadReq 批量上传请求
     * @param multipartFile MultipartFile 当前文件
     * @return FileUploadReq 单文件请求
     */
    private FileUploadReq buildSingleUploadReq(FileBatchUploadReq req,
            MultipartFile multipartFile) {
        var singleReq = new FileUploadReq();
        singleReq.setFile(multipartFile);
        singleReq.setRelationType(req.relationType());
        singleReq.setRelationId(req.relationId());
        singleReq.setRelationName(req.relationName());
        singleReq.setAttachmentType(req.attachmentType());
        singleReq.setIsPublic(req.isPublic());
        return singleReq;
    }

    /**
     * 上传文件到对象存储
     *
     * @param req        FileUploadReq 上传请求
     * @param attachment SysAttachment 附件实体
     */
    private void uploadToObjectStorage(FileUploadReq req, SysAttachment attachment) {
        uploadToMinio(req.getFile(), attachment.getFilePath(), attachment.getMimeType(), attachment.getFileSize());
    }

    /**
     * 持久化附件数据
     *
     * @param attachment SysAttachment 附件实体
     * @return Long 附件ID
     */
    private Long persistAttachment(SysAttachment attachment) {
        var attachmentId = sysAttachmentRepo.save(attachment);
        if (attachmentId == null) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "附件保存失败");
        }
        attachment.setId(attachmentId);
        return attachmentId;
    }

    /**
     * 解析原始文件名
     *
     * @param multipartFile MultipartFile 上传文件
     * @return String 原始文件名
     */
    private String resolveOriginalName(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return "";
        }
        return StringUtils.hasText(multipartFile.getOriginalFilename())
                ? multipartFile.getOriginalFilename()
                : multipartFile.getName();
    }

    /**
     * 从对象存储下载文件并封装上下文
     *
     * @param attachmentId Long 附件ID
     * @param attachment   SysAttachment 附件实体
     * @return FileDownloadContext 下载上下文
     */
    private FileDownloadContext downloadFromObjectStorage(Long attachmentId, SysAttachment attachment) {
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
     * 删除对象存储中的文件
     *
     * @param attachmentId Long 附件ID
     * @param attachment   SysAttachment 附件实体
     */
    private void removeObjectFromStorage(Long attachmentId, SysAttachment attachment) {
        ensureBucket();
        try {
            MinioUtil.deleteObject(minioClient, bucketName, attachment.getFilePath());
        } catch (Exception exception) {
            log.error(String.format("删除 MinIO 对象失败: attachmentId=%s", attachmentId), exception);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "删除存储对象失败");
        }
    }

    /**
     * 输入流提供者
     */
    @FunctionalInterface
    private interface InputStreamProvider {
        InputStream provide() throws IOException;
    }
}
