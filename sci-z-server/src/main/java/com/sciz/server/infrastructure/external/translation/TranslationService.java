package com.sciz.server.infrastructure.external.translation;

/**
 * 翻译服务接口
 *
 * @author JiaWen.Wu
 * @className TranslationService
 * @date 2025-01-24 14:30
 */
public interface TranslationService {
    /**
     * 翻译文本
     *
     * @param text 要翻译的文本
     * @param sourceLanguage 源语言代码（如：en, zh）
     * @param targetLanguage 目标语言代码（如：zh, en）
     * @return 翻译后的文本
     */
    String translate(String text, String sourceLanguage, String targetLanguage);
}

