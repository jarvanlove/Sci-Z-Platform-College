package com.sciz.server.domain.pojo.repository.knowledge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;

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
     * 根据Dify知识库ID查询知识库
     *
     * @param difyKnowdataId Dify知识库ID
     * @return 知识库实体
     */
    SysKnowledgeBase findByDifyKnowdataId(String difyKnowdataId);

    /**
     * 分页查询知识库列表
     *
     * @param page 分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @return 分页结果
     */
    IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId);

    /**
     * 根据ID删除知识库（软删除）
     *
     * @param id 知识库ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
}
