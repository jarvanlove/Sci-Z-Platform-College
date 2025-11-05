package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 登录用户基本信息响应
 *
 * @author JiaWen.Wu
 * @className ProfileResp
 * @date 2025-10-30 12:00
 */
public class ProfileResp {
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** token 名称 */
    private String tokenName;
    /** token 值 */
    private String tokenValue;
    /** token 剩余秒 */
    private long tokenTimeout;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public long getTokenTimeout() {
        return tokenTimeout;
    }

    public void setTokenTimeout(long tokenTimeout) {
        this.tokenTimeout = tokenTimeout;
    }
}
