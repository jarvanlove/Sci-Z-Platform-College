package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 文件转换器
 *
 * <p>
 * 负责附件实体与响应对象之间的映射。
 * </p>
 *
 * @author JiaWen.Wu
 * @className FileConverter
 * @date 2025-11-12 17:30
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileConverter {

    /**
     * SysAttachment → FileInfoResp
     *
     * @param entity SysAttachment 实体
     * @return FileInfoResp 响应
     */
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "previewUrl", ignore = true)
    FileInfoResp toInfoResp(SysAttachment entity);
}
