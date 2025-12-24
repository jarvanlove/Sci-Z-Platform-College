package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.literature.LiteratureSearchService;
import com.sciz.server.domain.pojo.dto.request.literature.LiteratureSearchReq;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureDetailResp;
import com.sciz.server.domain.pojo.dto.response.literature.LiteratureSearchPageResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文献搜索控制器
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchController
 * @date 2025-01-24 14:30
 */
@RestController
@RequestMapping("/api/literature")
@Tag(name = "文献搜索", description = "文献搜索相关接口")
@RequiredArgsConstructor
public class LiteratureSearchController {

    private final LiteratureSearchService literatureSearchService;

    /**
     * 搜索文献
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文献", description = "根据关键词搜索文献")
    public Result<LiteratureSearchPageResp> search(LiteratureSearchReq request) {
        LiteratureSearchPageResp result = literatureSearchService.search(request);
        return Result.success(result);
    }

}

