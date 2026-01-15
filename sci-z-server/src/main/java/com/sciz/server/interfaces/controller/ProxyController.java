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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    @Operation(summary = "获取缓存的PDF文件", description = "根据id或URL获取PDF文件流，用于下载，请求超时时间为10秒")
    @GetMapping("/pdf")
    public void getCachedPdf(@RequestParam(required = false) String url,
                              @RequestParam(required = false) String id,
                              HttpServletResponse response) {
        InputStream inputStream = null;
        
        try {
            String actualId = id;
            
            // 如果提供了URL但没有id，先缓存（带超时控制）
            if (url != null && !url.isEmpty() && (id == null || id.isEmpty())) {
                try {
                    // 使用 CompletableFuture 实现超时控制（10秒）
                    CompletableFuture<PdfCacheService.PdfCacheResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            return pdfCacheService.downloadAndCache(url);
                        } catch (BusinessException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    
                    PdfCacheService.PdfCacheResult result = future.get(10, TimeUnit.SECONDS);
                    actualId = result.id();
                    log.info("PDF缓存检查完成: url={}, id={}", url, actualId);
                } catch (TimeoutException e) {
                    log.warn("PDF下载超时（10秒）: url={}", url);
                    writeErrorResponse(response, HttpStatus.REQUEST_TIMEOUT.value(), 
                            "当前连接需要认证，请求超时，请手动下载", true);
                    return;
                } catch (Exception e) {
                    // 如果是 BusinessException 包装的异常，需要解包
                    Throwable cause = e.getCause();
                    if (cause instanceof BusinessException) {
                        BusinessException be = (BusinessException) cause;
                        log.warn("PDF下载失败: url={}, err={}", url, be.getMessage());
                        writeErrorResponse(response, be.getCode(), be.getMessage(), 
                                be.getCode() == ResultCode.FORBIDDEN.getCode());
                        return;
                    }
                    throw e;
                }
            }
            
            // 验证id
            if (actualId == null || actualId.isEmpty()) {
                writeErrorResponse(response, HttpStatus.BAD_REQUEST.value(), 
                        "id或URL不能为空", false);
                return;
            }

            // 获取PDF输入流（带超时控制）
            // 使用 final 变量供 lambda 使用
            final String finalActualId = actualId;
            try {
                // 使用 CompletableFuture 实现超时控制（10秒）
                CompletableFuture<InputStream> inputStreamFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return pdfCacheService.getCachedPdf(finalActualId);
                    } catch (BusinessException e) {
                        throw new RuntimeException(e);
                    }
                });
                
                inputStream = inputStreamFuture.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("获取PDF文件超时（10秒）: id={}", finalActualId);
                writeErrorResponse(response, HttpStatus.REQUEST_TIMEOUT.value(), 
                        "当前连接需要认证，请求超时，请手动下载", true);
                return;
            } catch (Exception e) {
                // 如果是 BusinessException 包装的异常，需要解包
                Throwable cause = e.getCause();
                if (cause instanceof BusinessException) {
                    BusinessException be = (BusinessException) cause;
                    log.warn("获取缓存的PDF文件失败: id={}, err={}", finalActualId, be.getMessage());
                    writeErrorResponse(response, be.getCode(), be.getMessage(), 
                            be.getCode() == ResultCode.FORBIDDEN.getCode());
                    return;
                }
                throw e;
            }
            
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
     * 
     * @param response HttpServletResponse 响应对象
     * @param status int HTTP状态码
     * @param message String 错误消息
     * @param requiresAuth boolean 是否需要认证（true表示需要手动下载）
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message, boolean requiresAuth) {
        try {
            // 确保响应头已设置，避免前端无法解析
            response.reset(); // 重置响应，清除之前可能设置的响应头
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", status);
            errorResponse.put("message", message);
            errorResponse.put("requiresAuth", requiresAuth);
            errorResponse.put("success", false);
            
            // 写入JSON响应
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            
            log.info("返回错误响应: status={}, message={}, requiresAuth={}", status, message, requiresAuth);
        } catch (Exception ex) {
            log.error("发送错误响应失败", ex);
            // 如果JSON写入失败，尝试返回纯文本
            try {
                response.reset();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("服务器错误：无法生成错误响应");
                response.getWriter().flush();
            } catch (Exception e) {
                log.error("发送纯文本错误响应也失败", e);
            }
        }
    }
}

