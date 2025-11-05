package com.sciz.server.interfaces.converter;

import org.mapstruct.Mapper;

/**
 * @author JiaWen.Wu
 * @className ProjectConverter
 * @date 2025-10-28 00:00
 */
@Mapper(componentModel = "spring")
public interface ProjectConverter {
    /**
     * 占位转换方法
     *
     * @param source Object 源
     * @return Object 目标
     */
    default Object convert(Object source) { return source; }
}


