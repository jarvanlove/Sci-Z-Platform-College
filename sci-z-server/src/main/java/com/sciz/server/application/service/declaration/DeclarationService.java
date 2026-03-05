package com.sciz.server.application.service.declaration;

import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationListQueryReq;
import com.sciz.server.domain.pojo.dto.request.file.FileUploadReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationUpdateStatusReq;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.domain.pojo.dto.response.file.FileInfoResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 申报应用服务
 * 
 * @author JiaWen.Wu
 * @className DeclarationService
 * @date 2025-01-20 15:00
 */
public interface DeclarationService {

    /**
     * 创建申报
     *
     * @param req 创建请求
     * @return 申报ID
     */
    Long create(DeclarationCreateReq req);

    /**
     * 分页查询申报列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<DeclarationListResp> page(DeclarationListQueryReq req);

    /**
     * 获取申报详情
     *
     * @param id 申报ID
     * @return 申报详情
     */
    DeclarationDetailResp findDetail(Long id);

    /**
     * 获取工作流状态
     *
     * @param id 申报ID
     * @return 工作流状态信息
     */
    DeclarationDetailResp.WorkflowResult getWorkflowStatus(Long id);

    /**
     * 上传红头文件
     *
     * @param req 文件上传请求
     * @return 红头文件解析响应（包含研究领域、研究方向、研究课题）
     */
    RedHeaderFileParseResp uploadRedHeaderFile(FileUploadReq req);

    /**
     * 更新申报状态
     *
     * @param req 更新状态请求
     */
    void updateStatus(DeclarationUpdateStatusReq req);

    /**
     * 上传/覆盖申报书文档
     * 上传新文件后，将该申报对应的附件关联更新为新附件ID（覆盖逻辑），下载时获取最新文件。
     *
     * @param declarationId 申报ID
     * @param file          申报书文件
     * @return 新附件信息（含 attachmentId）
     */
    FileInfoResp uploadDeclarationDocument(Long declarationId, MultipartFile file);
}
