package com.server.agentbackendservices.modules.dify.mapper;

import com.server.agentbackendservices.modules.common.mapper.MyBaseMapper;
import com.server.agentbackendservices.modules.dify.entity.DifyApiKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dify API 密钥 Mapper 接口
 *
 * @author shihang.shang
 * @className DifyApiKeyMapper
 * @date 2025-01-28 12:30
 */
@Mapper
public interface DifyApiKeyMapper extends MyBaseMapper<DifyApiKey> {

}
