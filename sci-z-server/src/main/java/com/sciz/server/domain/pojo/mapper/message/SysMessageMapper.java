package com.sciz.server.domain.pojo.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.message.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息 Mapper
 *
 * @author Sci-Z
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}
