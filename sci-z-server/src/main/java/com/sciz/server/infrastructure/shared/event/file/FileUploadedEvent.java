package com.sciz.server.infrastructure.shared.event.file;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传事件
 *
 * @author JiaWen.Wu
 * @className FileUploadedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class FileUploadedEvent extends DomainEvent {

    private String fileId;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String filePath;
    private String bucketName;
    private String userId;
    private String userName;
    private String projectId;
    private String projectName;
    private String uploadTime;
    private String fileHash;

    public FileUploadedEvent(String fileId, String fileName, String fileType,
            String fileSize, String filePath, String bucketName,
            String userId, String userName, String projectId,
            String projectName, String uploadTime, String fileHash) {
        super();
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.bucketName = bucketName;
        this.userId = userId;
        this.userName = userName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.uploadTime = uploadTime;
        this.fileHash = fileHash;
    }

    @Override
    public String getAggregateId() {
        return fileId;
    }

    @Override
    public String getAggregateType() {
        return "File";
    }
}
