package com.sciz.server.infrastructure.external.dify.dto.request;

/**
 * 字符串输入值
 *
 * @param value String 字符串值
 * @author JiaWen.Wu
 * @className DifyStringInput
 * @date 2025-01-26 15:00
 */
public record DifyStringInput(String value) implements DifyInputValue {
}
