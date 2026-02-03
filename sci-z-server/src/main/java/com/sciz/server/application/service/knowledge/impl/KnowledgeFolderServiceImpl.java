package com.sciz.server.application.service.knowledge.impl;

import com.sciz.server.application.service.knowledge.KnowledgeFolderService;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderResp;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFolderRepo;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeFileRelationRepo;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.KnowledgeFolderConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库文件夹应用服务实现类
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderServiceImpl
 * @date 2025-01-28 16:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeFolderServiceImpl implements KnowledgeFolderService {

    private final SysKnowledgeFolderRepo folderRepo;
    private final SysKnowledgeBaseRepo knowledgeBaseRepo;
    private final KnowledgeFolderConverter folderConverter;
    private final SysKnowledgeFileRelationRepo fileRelationRepo;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFolderResp create(KnowledgeFolderCreateReq req) {
        // 🔥 修复：确保parentId处理正确（null或0都表示根目录）
        Long actualParentId = (req.getParentId() == null || req.getParentId() == 0) ? 0L : req.getParentId();
        
        log.info(String.format("创建知识库文件夹: knowledgeId=%s, parentId=%s, actualParentId=%s, folderName=%s",
                req.getKnowledgeId(), req.getParentId(), actualParentId, req.getFolderName()));

        // 1. 校验知识库是否存在
        var knowledge = knowledgeBaseRepo.findById(req.getKnowledgeId());
        if (knowledge == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        // 2. 如果 parentId 不为 0，校验父文件夹是否存在
        if (actualParentId != null && actualParentId != 0) {
            var parentFolder = folderRepo.findById(actualParentId);
            if (parentFolder == null) {
                throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND, "父文件夹不存在");
            }
            // 校验父文件夹是否属于该知识库
            if (!parentFolder.getKnowledgeId().equals(req.getKnowledgeId())) {
                throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND, "父文件夹不属于该知识库");
            }
        }

        // 3. 校验同级目录下文件夹名称是否重复
        var existingFolder = folderRepo.findByKnowledgeIdAndParentIdAndFolderName(
                req.getKnowledgeId(), actualParentId, req.getFolderName().trim());
        if (existingFolder != null) {
            log.warn(String.format("文件夹名称已存在: knowledgeId=%s, parentId=%s, folderName=%s",
                    req.getKnowledgeId(), actualParentId, req.getFolderName()));
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NAME_DUPLICATE,
                    "同级目录下文件夹名称已存在: " + req.getFolderName());
        }

        // 4. 构建文件夹路径
        var folderPath = buildFolderPath(req.getKnowledgeId(), actualParentId, req.getFolderName().trim());

        // 5. 转换为实体
        var entity = folderConverter.toEntity(req);
        // 🔥 修复：确保保存的parentId是正确的值
        entity.setParentId(actualParentId);
        entity.setFolderPath(folderPath);
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        // 6. 保存
        var id = folderRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 7. 转换为响应
        var resp = folderConverter.toResp(entity);
        log.info(String.format("创建知识库文件夹成功: id=%s, parentId=%s", id, actualParentId));
        return resp;
    }

    @Override
    public KnowledgeFolderResp findDetail(Long id) {
        var folder = folderRepo.findById(id);
        if (folder == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND);
        }
        return folderConverter.toResp(folder);
    }

    @Override
    public List<KnowledgeFolderResp> getFolderTree(Long knowledgeId) {
        // 1. 校验知识库是否存在
        var knowledge = knowledgeBaseRepo.findById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        // 2. 查询所有文件夹
        var allFolders = folderRepo.findByKnowledgeId(knowledgeId);
        if (allFolders.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 转换为响应列表
        var folderMap = allFolders.stream()
                .map(folderConverter::toResp)
                .collect(Collectors.toMap(KnowledgeFolderResp::getId, folder -> folder));

        // 4. 构建树形结构
        var rootFolders = new ArrayList<KnowledgeFolderResp>();
        for (var folder : folderMap.values()) {
            if (folder.getParentId() == null || folder.getParentId() == 0) {
                // 根目录下的文件夹
                rootFolders.add(folder);
            } else {
                // 子文件夹，添加到父文件夹的 children 中
                var parent = folderMap.get(folder.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(folder);
                }
            }
        }

        return rootFolders;
    }

    @Override
    public List<KnowledgeFolderResp> listByParentId(Long knowledgeId, Long parentId) {
        // 1. 校验知识库是否存在
        var knowledge = knowledgeBaseRepo.findById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        // 2. 如果 parentId 不为 0，校验父文件夹是否存在
        if (parentId != null && parentId != 0) {
            var parentFolder = folderRepo.findById(parentId);
            if (parentFolder == null) {
                throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND, "父文件夹不存在");
            }
        }

        // 3. 查询文件夹列表
        // 🔥 修复：确保parentId处理正确（null或0都表示根目录）
        Long actualParentId = (parentId == null || parentId == 0) ? 0L : parentId;
        log.info(String.format("查询文件夹列表: knowledgeId=%s, parentId=%s, actualParentId=%s", 
                knowledgeId, parentId, actualParentId));
        var folders = folderRepo.findByKnowledgeIdAndParentId(knowledgeId, actualParentId);
        log.info(String.format("查询到文件夹数量: %d", folders.size()));
        return folderConverter.toRespList(folders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeFolderResp update(Long id, KnowledgeFolderUpdateReq req) {
        log.info(String.format("更新知识库文件夹: id=%s, folderName=%s", id, req.getFolderName()));

        // 1. 查询文件夹
        var folder = folderRepo.findById(id);
        if (folder == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND);
        }

        // 2. 如果文件夹名称改变，校验同级目录下是否重名
        if (!folder.getFolderName().equals(req.getFolderName().trim())) {
            var existingFolder = folderRepo.findByKnowledgeIdAndParentIdAndFolderName(
                    folder.getKnowledgeId(), folder.getParentId(), req.getFolderName().trim());
            if (existingFolder != null && !existingFolder.getId().equals(id)) {
                log.warn(String.format("文件夹名称已存在: knowledgeId=%s, parentId=%s, folderName=%s",
                        folder.getKnowledgeId(), folder.getParentId(), req.getFolderName()));
                throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NAME_DUPLICATE,
                        "同级目录下文件夹名称已存在: " + req.getFolderName());
            }

            // 3. 更新文件夹路径
            var newFolderPath = buildFolderPath(folder.getKnowledgeId(), folder.getParentId(), req.getFolderName().trim());
            folder.setFolderPath(newFolderPath);
        }

        // 4. 更新实体
        folderConverter.updateEntity(folder, req);
        if (req.getSortOrder() != null) {
            folder.setSortOrder(req.getSortOrder());
        }

        // 5. 保存
        var updated = folderRepo.updateById(folder);
        if (!updated) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 6. 转换为响应
        var resp = folderConverter.toResp(folder);
        log.info(String.format("更新知识库文件夹成功: id=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info(String.format("删除知识库文件夹: id=%s", id));

        // 1. 查询文件夹
        var folder = folderRepo.findById(id);
        if (folder == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_NOT_FOUND);
        }

        // 2. 检查是否有子文件夹
        var childCount = folderRepo.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_HAS_CHILDREN,
                    "文件夹下存在子文件夹，无法删除");
        }

        // 3. 检查文件夹下是否有文件
        var fileCount = fileRelationRepo.countByFolderId(id);
        if (fileCount > 0) {
            throw new BusinessException(ResultCode.KNOWLEDGE_FOLDER_HAS_FILES,
                    "文件夹下存在文件，无法删除");
        }

        // 4. 软删除
        var deleted = folderRepo.deleteById(id);
        if (!deleted) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("删除知识库文件夹成功: id=%s", id));
    }

    /**
     * 构建文件夹路径
     *
     * @param knowledgeId 知识库ID
     * @param parentId 父文件夹ID
     * @param folderName 文件夹名称
     * @return 文件夹路径
     */
    private String buildFolderPath(Long knowledgeId, Long parentId, String folderName) {
        if (parentId == null || parentId == 0) {
            // 根目录下的文件夹
            return "/" + folderName;
        }

        // 查询父文件夹路径
        var parentFolder = folderRepo.findById(parentId);
        if (parentFolder == null) {
            return "/" + folderName;
        }

        // 拼接路径
        var parentPath = parentFolder.getFolderPath();
        if (parentPath.endsWith("/")) {
            return parentPath + folderName;
        } else {
            return parentPath + "/" + folderName;
        }
    }
}

