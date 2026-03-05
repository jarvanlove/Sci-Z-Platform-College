package com.sciz.server.domain.pojo.dto.request.practice;

import jakarta.validation.constraints.NotBlank;

/**
 * 产教研分发拒绝请求
 *
 * @param reason 拒绝原因
 */
public record DistributeRejectReq(
        @NotBlank(message = "拒绝原因不能为空") String reason) {
}
