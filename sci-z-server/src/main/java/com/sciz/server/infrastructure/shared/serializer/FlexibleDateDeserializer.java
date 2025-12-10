package com.sciz.server.infrastructure.shared.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sciz.server.infrastructure.shared.utils.DateUtil;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 灵活的日期反序列化器
 * <p>
 * 支持多种日期格式：
 * - "yyyy-MM-dd"（标准格式）
 * - "yyyy-MM-dd HH:mm:ss"（日期时间格式，自动提取日期部分）
 *
 * @author JiaWen.Wu
 * @className FlexibleDateDeserializer
 * @date 2025-12-10 15:00
 */
public class FlexibleDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return DateUtil.parseDateFlexible(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidFormatException(p, e.getMessage(), value, LocalDate.class);
        }
    }
}
