package com.sciz.server.infrastructure.external.dify.dto.request;

import java.util.List;

/**
 * 字符串数组输入值
 *
 * @param values List<String> 字符串列表
 * @author JiaWen.Wu
 * @className DifyStringArrayInput
 * @date 2025-01-26 15:00
 */
public record DifyStringArrayInput(List<String> values) implements DifyInputValue {
}
