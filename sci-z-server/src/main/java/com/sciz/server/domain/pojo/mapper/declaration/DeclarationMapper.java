package com.sciz.server.domain.pojo.mapper.declaration;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申报 Mapper
 *
 * @author JiaWen.Wu
 * @className DeclarationMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface DeclarationMapper extends BaseMapper<Declaration> {
}
