//package com.server.agentbackendservices.modules.config;
//
//import cn.dev33.satoken.stp.StpUtil;
//import com.server.agentbackendservices.modules.common.enums.StatusCodeEnum;
//import com.server.agentbackendservices.modules.common.exception.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
///**
// * 自定义Token校验拦截器
// * 重写token校验逻辑，提供更详细的错误信息
// *
// * @author AgentBackendServices
// * @since 2024-01-01
// */
//@Slf4j
//@Component
//public class TokenValidationInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        log.info("开始Token校验 - 请求路径: {}", request.getRequestURI());
//
//        try {
//            // 1. 检查是否有token
//            String token = getTokenFromRequest(request);
//            if (token == null || token.trim().isEmpty()) {
//                log.warn("Token校验失败: 未提供token - 请求路径: {}", request.getRequestURI());
//                throw new BusinessException(StatusCodeEnum.NO_LOGIN, "请先登录，未提供访问令牌");
//            }
//
//            // 2. 检查token是否有效
//            if (!StpUtil.isLogin()) {
//                log.warn("Token校验失败: token无效或已过期 - 请求路径: {}", request.getRequestURI());
//                throw new BusinessException(StatusCodeEnum.TOKEN_INVALID, "访问令牌无效或已过期，请重新登录");
//            }
//
//            // 3. 检查token是否过期
//            long timeout = StpUtil.getTokenTimeout();
//            if (timeout <= 0) {
//                log.warn("Token校验失败: token已过期 - 请求路径: {}", request.getRequestURI());
//                throw new BusinessException(StatusCodeEnum.TOKEN_EXPIRED, "访问令牌已过期，请重新登录");
//            }
//
//            // 4. 获取当前用户信息
//            Long userId = StpUtil.getLoginIdAsLong();
//            log.info("Token校验成功 - 用户ID: {}, 剩余时间: {}秒, 请求路径: {}", userId, timeout, request.getRequestURI());
//
//            return true;
//
//        } catch (BusinessException e) {
//            log.error("Token校验异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
//            throw e;
//        } catch (Exception e) {
//            log.error("Token校验系统异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
//            throw new BusinessException(StatusCodeEnum.SYSTEM_ERROR, "Token校验系统异常");
//        }
//    }
//
//    /**
//     * 从请求中获取token
//     * 支持从Header、Cookie、请求体中获取token
//     */
//    private String getTokenFromRequest(HttpServletRequest request) {
//        // 1. 从Authorization头获取token
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//
//        // 2. 从Cookie获取token
//        String tokenFromCookie = getTokenFromCookie(request);
//        if (tokenFromCookie != null) {
//            return tokenFromCookie;
//        }
//
//        // 3. 从请求参数获取token
//        String tokenFromParam = request.getParameter("token");
//        if (tokenFromParam != null && !tokenFromParam.trim().isEmpty()) {
//            return tokenFromParam;
//        }
//
//        return null;
//    }
//
//    /**
//     * 从Cookie中获取token
//     */
//    private String getTokenFromCookie(HttpServletRequest request) {
//        if (request.getCookies() != null) {
//            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
//                if ("satoken".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
//}
