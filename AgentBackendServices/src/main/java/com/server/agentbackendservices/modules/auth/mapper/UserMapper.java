package com.server.agentbackendservices.modules.auth.mapper;




import com.server.agentbackendservices.modules.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.server.agentbackendservices.mapper.MyBaseMapper;

@Mapper
public interface UserMapper extends MyBaseMapper<User> {
    // 可以添加其他用户相关的SQL方法
}