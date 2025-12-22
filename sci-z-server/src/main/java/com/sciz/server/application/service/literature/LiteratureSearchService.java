package com.sciz.server.application.service.literature;

import com.sciz.server.domain.pojo.dto.request.literature.LiteratureSearchReq;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureDetailResp;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureSearchPageResp;

/**
 * 文献搜索服务接口
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchService
 * @date 2025-01-24 14:30
 */
public interface LiteratureSearchService {
    /**
     * 搜索文献
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    LiteratureSearchPageResp search(LiteratureSearchReq request);


}

