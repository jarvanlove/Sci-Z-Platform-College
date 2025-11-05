package com.sciz.server.domain.pojo.repository.declaration;

import com.sciz.server.domain.pojo.entity.declaration.Declaration;

/**
 * 申报仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className DeclarationRepo
 * @date 2025-10-30 11:00
 */
public interface DeclarationRepo {

    /**
     * 保存申报
     *
     * @param entity Declaration 实体
     * @return 生成的主键ID
     */
    Long save(Declaration entity);
}
