package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import org.mapstruct.Mapper;

/**
 * @author JiaWen.Wu
 * @className AuthConverter
 * @date 2025-10-30 00:00
 */
@Mapper(componentModel = "spring")
public interface AuthConverter {

    /**
     * 构造登录返回模型
     *
     * @param user       SysUser 登录用户
     * @param token      String 访问令牌
     * @param expireTime Long 过期秒数
     * @return LoginResp 登录响应
     */
    default LoginResp toLoginResp(SysUser user, String token, Long expireTime) {
        if (user == null) {
            return null;
        }
        LoginResp resp = new LoginResp();
        //resp.setUserId(user.getId());
        //resp.setUsername(user.getUsername());
        //resp.setToken(token);
        //resp.setExpireTime(expireTime);
        // 如需可扩展 realName、refreshToken 等，在此补充
        return resp;
    }

    /**
     * 构造个人信息返回模型
     *
     * @param userId       Long 用户ID
     * @param username     String 用户名
     * @param tokenName    String Token 名称
     * @param tokenValue   String Token 值
     * @param tokenTimeout Long Token 剩余秒数
     * @return ProfileResp 个人信息响应
     */
    default ProfileResp toProfileResp(Long userId, String username, String tokenName, String tokenValue,
            Long tokenTimeout) {
        ProfileResp resp = new ProfileResp();
        resp.setUserId(userId);
        resp.setUsername(username);
        resp.setTokenName(tokenName);
        resp.setTokenValue(tokenValue);
        resp.setTokenTimeout(tokenTimeout);
        return resp;
    }
}
