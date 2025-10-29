package com.sciz.server.infrastructure.common.filter;

import com.sciz.server.infrastructure.shared.utils.TraceIdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author JiaWen.Wu
 * @className TraceFilter
 * @date 2025-10-28 00:00
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter implements Filter {

    /**
     * 过滤请求，写入并透传 TraceId
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤链
     * @return void
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String traceId = req.getHeader("X-Trace-Id");
        if (StringUtils.isBlank(traceId)) {
            traceId = TraceIdUtil.generateTraceId();
        }
        TraceIdUtil.setTraceId(traceId);
        resp.setHeader("X-Trace-Id", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            TraceIdUtil.clear();
        }
    }
}
