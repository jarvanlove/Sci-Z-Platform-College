package com.sciz.server.infrastructure.external.dify.dto.request;

import java.util.List;

/**
 * 文件数组输入值
 *
 * @param files List<DifyFileInput> 文件列表
 * @author JiaWen.Wu
 * @className DifyFileArrayInput
 * @date 2025-01-26 15:00
 */
public record DifyFileArrayInput(List<DifyFileInput> files) implements DifyInputValue {
}
