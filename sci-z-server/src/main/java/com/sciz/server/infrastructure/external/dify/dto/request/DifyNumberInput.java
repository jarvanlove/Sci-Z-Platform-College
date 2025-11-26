package com.sciz.server.infrastructure.external.dify.dto.request;

/**
 * 数字输入值
 *
 * @param value Number 数字值
 * @author JiaWen.Wu
 * @className DifyNumberInput
 * @date 2025-01-26 15:00
 */
public record DifyNumberInput(Number value) implements DifyInputValue {
}
