package com.sciz.server.infrastructure.shared.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sciz.server.infrastructure.shared.utils.DateUtil;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 灵活的日期时间反序列化器
 * <p>
 * 支持多种日期时间格式：
 * - "yyyy-MM-dd HH:mm:ss"（标准格式）
 * - "yyyy-MM-dd"（日期格式，自动补充时间为 00:00:00）
 *
 * @author JiaWen.Wu
 * @className FlexibleDateTimeDeserializer
 * @date 2025-12-10 15:00
 */
public class FlexibleDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return DateUtil.parseDateTimeFlexible(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidFormatException(p, e.getMessage(), value, LocalDateTime.class);
        }
    }
}
