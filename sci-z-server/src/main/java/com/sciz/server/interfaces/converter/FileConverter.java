package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.domain.pojo.entity.file.SysAttachment;
import org.springframework.stereotype.Component;

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
@Component
public class FileConverter {

    /**
     * SysAttachment → FileInfoResp
     *
     * @param entity SysAttachment 实体
     * @return FileInfoResp 响应
     */
    public FileInfoResp toInfoResp(SysAttachment entity) {
        if (entity == null) {
            return null;
        }
        return new FileInfoResp(
                entity.getId(),
                entity.getFileName(),
                entity.getOriginalName(),
                entity.getFileType(),
                entity.getFileExtension(),
                entity.getFileSize(),
                entity.getMimeType(),
                entity.getFileUrl(),
                entity.getFilePath(),
                entity.getMd5Hash(),
                entity.getIsPublic(),
                entity.getDownloadCount(),
                entity.getUploaderId(),
                entity.getUploaderName(),
                entity.getUploadTime(),
                null,
                null);
    }
}
