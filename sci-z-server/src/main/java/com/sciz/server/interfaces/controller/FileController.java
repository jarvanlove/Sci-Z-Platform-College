package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.file.FileService;
import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.file.FileCheckDuplicateReq;
import com.sciz.server.domain.pojo.dto.request.file.FileListQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.file.FileDuplicateCheckResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.dto.response.file.FileDownloadContext;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件管理控制器
 *
 * @author JiaWen.Wu
 * @className FileController
 * @date 2025-10-28 00:00
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件管理", description = "文件管理相关接口")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "文件上传", description = "单文件上传")
    @PostMapping("/upload")
    public Result<FileInfoResp> upload(@Valid @ModelAttribute FileUploadReq req) {
        FileInfoResp resp = fileService.upload(req);
        return Result.success(resp, ResultCode.FILE_UPLOAD_SUCCESS.getMessage());
    }

    @Operation(summary = "批量文件上传", description = "多文件上传")
    @PostMapping("/batch-upload")
    public Result<List<FileInfoResp>> batchUpload(@Valid @ModelAttribute FileBatchUploadReq req) {
        List<FileInfoResp> respList = fileService.uploadBatch(req);
        return Result.success(respList, ResultCode.FILE_UPLOAD_SUCCESS.getMessage());
    }

    @Operation(summary = "文件列表", description = "分页查询文件列表")
    @GetMapping("/list")
    public Result<PageResult<FileInfoResp>> list(@ModelAttribute FileListQueryReq req) {
        PageResult<FileInfoResp> pageResult = fileService.page(req);
        return Result.success(pageResult);
    }

    @Operation(summary = "文件下载", description = "根据ID下载文件")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        try (FileDownloadContext context = fileService.download(id);
                var inputStream = context.inputStream()) {
            response.setContentType(context.contentType());
            if (context.contentLength() != null) {
                response.setContentLengthLong(context.contentLength());
            }
            String encodedName = URLEncoder.encode(context.originalName(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    String.format("attachment; filename=%s; filename*=UTF-8''%s", encodedName, encodedName));
            StreamUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error(String.format("文件下载失败: attachmentId=%s", id), e);
            throw new BusinessException(ResultCode.FILE_DOWNLOAD_FAILED, "文件下载失败");
        }
    }

    @Operation(summary = "文件预览", description = "根据ID获取预览签名链接")
    @GetMapping("/preview/{id}")
    public Result<String> preview(@PathVariable Long id, @RequestParam(required = false) Integer expireSeconds) {
        String previewUrl = fileService.preview(id, expireSeconds);
        return Result.success(previewUrl, ResultCode.FILE_DOWNLOAD_SUCCESS.getMessage());
    }

    @Operation(summary = "删除文件", description = "根据ID删除文件")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success(ResultCode.FILE_DELETE_SUCCESS.getMessage());
    }

    @Operation(summary = "文件去重检查", description = "根据MD5检查文件是否已存在")
    @PostMapping("/check-duplicate")
    public Result<FileDuplicateCheckResp> checkDuplicate(@Valid @RequestBody FileCheckDuplicateReq req) {
        FileDuplicateCheckResp resp = fileService.checkDuplicate(req);
        return Result.success(resp);
    }

    @Operation(summary = "同步到Dify", description = "文件同步到Dify知识库")
    @PostMapping("/sync-dify")
    public Result<Object> syncDify(@RequestBody Object request) {
        return Result.success(null);
    }
}
