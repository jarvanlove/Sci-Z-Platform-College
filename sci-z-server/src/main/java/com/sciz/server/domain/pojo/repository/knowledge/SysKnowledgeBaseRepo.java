package com.sciz.server.domain.pojo.repository.knowledge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;

import java.util.List;
import java.util.Map;

/**
 * 知识库仓储（领域层抽象）
 * 
 * @author ShiHang.Shang
 * @className SysKnowledgeBaseRepo
 * @date 2025-10-30 11:00
 */
public interface SysKnowledgeBaseRepo {

    /**
     * 保存知识库
     *
     * @param entity SysKnowledgeBase 实体
     * @return 生成的主键ID
     */
    Long save(SysKnowledgeBase entity);

    /**
     * 根据ID查询知识库
     *
     * @param id 知识库ID
     * @return 知识库实体
     */
    SysKnowledgeBase findById(Long id);

    /**
     * 根据名称查询知识库
     *
     * @param name 知识库名称
     * @return 知识库实体
     */
    SysKnowledgeBase findByName(String name);

    /**
     * 根据Dify知识库ID查询知识库
     *
     * @param difyKnowdataId Dify知识库ID（字符串类型）
     * @return 知识库实体
     */
    SysKnowledgeBase findByDifyKnowdataId(int difyKnowdataId);

    /**
     * 分页查询知识库列表
     *
     * @param page   分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @return 分页结果
     */
    IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId);

    /**
     * 分页查询知识库列表（支持关键字搜索）
     *
     * @param page   分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @param keyword 搜索关键字（知识库名称/描述），可为null
     * @param sortBy 排序字段，可为null
     * @param asc 是否升序，false为降序
     * @return 分页结果
     */
    IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId, String keyword, String sortBy, boolean asc);

    /**
     * 分页查询知识库列表（支持类型筛选与项目成员可见性）
     * 普通用户可见：本人创建的 + 作为项目成员可见的项目知识库；管理员可见全部。
     *
     * @param page              分页对象
     * @param userId            当前用户ID（管理员可为null表示不限）
     * @param memberProjectIds  当前用户作为成员的项目ID列表（非管理员时必传，可为空列表）
     * @param kbType            类型筛选：personal / project / null=全部
     * @param keyword           搜索关键字，可为null
     * @param sortBy            排序字段，可为null
     * @param asc               是否升序
     * @return 分页结果
     */
    IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId, List<Long> memberProjectIds,
                                            String kbType, String keyword, String sortBy, boolean asc);

    /**
     * 根据ID删除知识库（软删除）
     *
     * @param id 知识库ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 更新知识库
     *
     * @param entity 知识库实体
     * @return 是否更新成功
     */
    boolean updateById(SysKnowledgeBase entity);

    /**
     * 更新知识库文件数量
     *
     * @param knowledgeId 知识库ID
     * @param fileCount   文件数量
     * @return 是否更新成功
     */
    boolean updateFileCount(Long knowledgeId, Integer fileCount);
//
//    /**
//     * 更新知识库文件夹数量
//     *
//     * @param knowledgeId 知识库ID
//     * @param folderCount 文件夹数量
//     * @return 是否更新成功
//     */
//    boolean updateFolderCount(Long knowledgeId, Integer folderCount);

    /**
     * 根据项目ID列表批量查询知识库
     *
     * @param projectIds 项目ID列表
     * @return Map<Long, SysKnowledgeBase> 项目ID -> 知识库实体映射
     */
    Map<Long, SysKnowledgeBase> findByProjectIds(List<Long> projectIds);
}
