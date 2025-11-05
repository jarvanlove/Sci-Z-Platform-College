package com.sciz.server.domain.pojo.mapper.knowledge;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文件关联 Mapper
 *
 * @author JiaWen.Wu
 * @className SysKnowledgeFileRelationMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysKnowledgeFileRelationMapper extends BaseMapper<SysKnowledgeFileRelation> {
}
