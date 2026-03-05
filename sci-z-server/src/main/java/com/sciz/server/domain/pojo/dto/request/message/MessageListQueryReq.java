package com.sciz.server.domain.pojo.dto.request.message;

import lombok.Data;

/**
 * 站内消息列表查询请求
 *
 * @author Sci-Z
 */
@Data
public class MessageListQueryReq {

    /** 当前页，从 1 开始 */
    private Integer page = 1;
    /** 每页条数 */
    private Integer size = 10;
    /** 是否仅未读 */
    private Boolean unreadOnly = false;

    public int getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int getSize() {
        if (size == null || size < 1) return 10;
        return Math.min(size, 100);
    }
}
