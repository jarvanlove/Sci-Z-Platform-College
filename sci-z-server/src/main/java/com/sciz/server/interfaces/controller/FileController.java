package com.sciz.server.interfaces.controller;

import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @author JiaWen.Wu
 * @className FileController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileController {

    @Operation(summary = "文件上传", description = "单文件上传")
    @PostMapping("/upload")
    public Result<Object> upload(@RequestParam("file") Object file,
            @RequestParam(required = false) String relationType,
            @RequestParam(required = false) Long relationId,
            @RequestParam(required = false) String attachmentType,
            @RequestParam(required = false, defaultValue = "0") Integer isPublic) {
        return Result.success(null);
    }

    @Operation(summary = "批量文件上传", description = "多文件上传")
    @PostMapping("/batch-upload")
    public Result<Object> batchUpload(@RequestParam("files") Object files,
            @RequestParam(required = false) String relationType,
            @RequestParam(required = false) Long relationId,
            @RequestParam(required = false) String attachmentType,
            @RequestParam(required = false, defaultValue = "0") Integer isPublic) {
        return Result.success(null);
    }

    @Operation(summary = "文件列表", description = "分页查询文件列表")
    @GetMapping("/list")
    public Result<Object> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(null);
    }

    @Operation(summary = "文件下载", description = "根据ID下载文件")
    @GetMapping("/download/{id}")
    public Result<Object> download(@PathVariable Long id) {
        return Result.success(null);
    }

    @Operation(summary = "文件预览", description = "根据ID获取预览信息或直链")
    @GetMapping("/preview/{id}")
    public Result<Object> preview(@PathVariable Long id) {
        return Result.success(null);
    }

    @Operation(summary = "删除文件", description = "根据ID删除文件")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return Result.success(null);
    }

    @Operation(summary = "文件去重检查", description = "根据MD5检查文件是否已存在")
    @PostMapping("/check-duplicate")
    public Result<Object> checkDuplicate(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "同步到Dify", description = "文件同步到Dify知识库")
    @PostMapping("/sync-dify")
    public Result<Object> syncDify(@RequestBody Object request) {
        return Result.success(null);
    }
}
