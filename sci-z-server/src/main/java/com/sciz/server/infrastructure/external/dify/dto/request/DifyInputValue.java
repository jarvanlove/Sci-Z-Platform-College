package com.sciz.server.infrastructure.external.dify.dto.request;

/**
 * Dify 工作流输入值的基础接口
 * 使用密封接口（Sealed Interface）限制实现类，提供类型安全
 *
 * @author JiaWen.Wu
 * @className DifyInputValue
 * @date 2025-01-26 15:00
 */
public sealed interface DifyInputValue
        permits DifyStringInput, DifyNumberInput, DifyFileInput, DifyFileArrayInput, DifyStringArrayInput {
}
