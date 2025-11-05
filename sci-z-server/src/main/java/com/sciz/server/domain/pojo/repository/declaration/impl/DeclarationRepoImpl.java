package com.sciz.server.domain.pojo.repository.declaration.impl;

import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.mapper.declaration.DeclarationMapper;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import org.springframework.stereotype.Repository;

/**
 * 申报仓储实现
 * 
 * @author JiaWen.Wu
 * @className DeclarationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class DeclarationRepoImpl implements DeclarationRepo {

    private final DeclarationMapper mapper;

    public DeclarationRepoImpl(DeclarationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(Declaration entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
