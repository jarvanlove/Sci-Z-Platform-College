package com.sciz.server.domain.pojo.dto.request.declaration;

import jakarta.validation.constraints.NotNull;

/**
 * 申报状态更新请求
 *
 * @param id     Long 申报ID
 * @param status Integer 申报状态（1=申报中，2=申报成功，3=申报失败）
 * @author JiaWen.Wu
 * @className DeclarationUpdateStatusReq
 * @date 2025-11-27 17:00
 */
public record DeclarationUpdateStatusReq(
        @NotNull(message = "申报ID不能为空") Long id,
        @NotNull(message = "申报状态不能为空") Integer status) {
}
