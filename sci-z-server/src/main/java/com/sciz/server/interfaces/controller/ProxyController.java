package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.pdf.PdfCacheService;
import com.sciz.server.domain.pojo.dto.response.proxy.PdfCacheResp;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理控制器
 * 用于代理下载外部资源（如PDF文件）
 * 使用 presignedGetUrl 作为预览 key，返回唯一 id 用于查询、预览、删除
 *
 * @author System
 * @className ProxyController
 * @date 2025-01-XX
 */
@Slf4j
@RestController
@RequestMapping("/api/proxy")
@Tag(name = "代理服务", description = "代理下载外部资源相关接口")
@RequiredArgsConstructor
public class ProxyController {

    private final PdfCacheService pdfCacheService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "PDF代理下载并缓存", description = "先查询Redis是否有存储文件key，如果有则直接返回，如果没有则下载并缓存，返回唯一id和presignedUrl")
    @GetMapping("/pdf/cache")
    public Result<PdfCacheResp> cachePdf(@RequestParam String url) {
        try {
            PdfCacheService.PdfCacheResult result = pdfCacheService.downloadAndCache(url);
            PdfCacheResp response = PdfCacheResp.builder()
                    .cacheKey(result.id()) // 返回id作为cacheKey
                    .filePath(result.presignedUrl()) // 返回presignedUrl作为filePath
                    .fileName(result.fileName())
                    .build();
            return Result.success(response, "PDF缓存成功");
        } catch (BusinessException e) {
            log.warn("PDF下载失败: url={}, err={}", url, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("PDF下载失败: url={}", url, e);
            return Result.fail(ResultCode.SERVER_ERROR, "PDF下载失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取PDF预览链接", description = "根据id重新生成presignedUrl预览链接")
    @GetMapping("/pdf/preview")
    public Result<String> previewPdf(@RequestParam String id) {
        try {
            String presignedUrl = pdfCacheService.getPreviewUrl(id);
            return Result.success(presignedUrl, "获取预览链接成功");
        } catch (BusinessException e) {
            log.warn("获取预览链接失败: id={}, err={}", id, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取预览链接失败: id={}", id, e);
            return Result.fail(ResultCode.SERVER_ERROR, "获取预览链接失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取缓存的PDF文件", description = "根据id或URL获取PDF文件流，用于下载")
    @GetMapping("/pdf")
    public void getCachedPdf(@RequestParam(required = false) String url,
                              @RequestParam(required = false) String id,
                              HttpServletResponse response) {
        InputStream inputStream = null;
        
        try {
            String actualId = id;
            
            // 如果提供了URL但没有id，先缓存
            if (url != null && !url.isEmpty() && (id == null || id.isEmpty())) {
                PdfCacheService.PdfCacheResult result = pdfCacheService.downloadAndCache(url);
                actualId = result.id();
                log.info("PDF缓存检查完成: url={}, id={}", url, actualId);
            }
            
            // 验证id
            if (actualId == null || actualId.isEmpty()) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST.value(), 
                        "id或URL不能为空", false);
                return;
            }

            // 获取PDF输入流
            inputStream = pdfCacheService.getCachedPdf(actualId);
            String fileName = pdfCacheService.getCachedPdfFileName(actualId);

            // 设置响应头
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            
            // 复制流
            StreamUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            
            log.info("返回缓存的PDF文件: id={}, fileName={}", actualId, fileName);
            
        } catch (BusinessException e) {
            log.warn("获取缓存的PDF文件失败: id={}, err={}", id, e.getMessage());
            writeErrorResponse(response, e.getCode(), e.getMessage(), 
                    e.getCode() == ResultCode.FORBIDDEN.getCode());
        } catch (Exception e) {
            log.error("获取缓存的PDF文件失败: id={}", id, e);
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "获取PDF文件失败: " + e.getMessage(), false);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.warn("关闭输入流失败", e);
                }
            }
        }
    }

    @Operation(summary = "删除PDF缓存", description = "根据id删除PDF缓存，包括Redis记录和MinIO文件")
    @DeleteMapping("/pdf")
    public Result<Void> deletePdfCache(@RequestParam String id) {
        try {
            pdfCacheService.deleteCache(id);
            return Result.success(null, "删除PDF缓存成功");
        } catch (BusinessException e) {
            log.warn("删除PDF缓存失败: id={}, err={}", id, e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除PDF缓存失败: id={}", id, e);
            return Result.fail(ResultCode.SERVER_ERROR, "删除PDF缓存失败: " + e.getMessage());
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message, boolean requiresAuth) {
        try {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", status);
            errorResponse.put("message", message);
            errorResponse.put("requiresAuth", requiresAuth);
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        } catch (Exception ex) {
            log.error("发送错误响应失败", ex);
        }
    }
}

