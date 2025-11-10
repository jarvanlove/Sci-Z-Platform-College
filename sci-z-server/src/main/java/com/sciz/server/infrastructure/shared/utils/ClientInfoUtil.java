package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.infrastructure.external.Ip2region.IpLocationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端信息工具类
 * 用于获取请求的客户端信息（IP、浏览器、操作系统等）
 *
 * @author JiaWen.Wu
 * @className ClientInfoUtil
 * @date 2025-11-06 10:20
 */
@Component
public class ClientInfoUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static IpLocationService ipLocationService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ClientInfoUtil.applicationContext = applicationContext;
        try {
            ClientInfoUtil.ipLocationService = applicationContext.getBean(IpLocationService.class);
        } catch (Exception ignored) {
            ClientInfoUtil.ipLocationService = null;
        }
    }

    private static final String UNKNOWN = "unknown";
    private static final Pattern BROWSER_PATTERN = Pattern.compile(
            "(Chrome|Firefox|Safari|Edge|Opera|MSIE|Trident)/([\\d.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OS_PATTERN = Pattern.compile(
            "(Windows|Mac|Linux|Android|iOS|iPhone|iPad|Unix)([\\s\\d._]*)?", Pattern.CASE_INSENSITIVE);

    public static String getClientIp(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(ClientInfoUtil::resolveClientIp)
                .orElse(UNKNOWN);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getClientIp() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(ClientInfoUtil::getClientIp)
                .orElse(UNKNOWN);
    }

    /**
     * 获取浏览器信息
     *
     * @param request HttpServletRequest
     * @return 浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(req -> req.getHeader("User-Agent"))
                .filter(ClientInfoUtil::hasText)
                .map(ClientInfoUtil::parseBrowser)
                .orElse(UNKNOWN);
    }

    /**
     * 获取浏览器信息
     *
     * @return 浏览器信息
     */
    public static String getBrowser() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(ClientInfoUtil::getBrowser)
                .orElse(UNKNOWN);
    }

    /**
     * 获取操作系统信息
     *
     * @param request HttpServletRequest
     * @return 操作系统信息
     */
    public static String getOs(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(req -> req.getHeader("User-Agent"))
                .filter(ClientInfoUtil::hasText)
                .map(ClientInfoUtil::parseOs)
                .orElse(UNKNOWN);
    }

    /**
     * 获取操作系统信息
     *
     * @return 操作系统信息
     */
    public static String getOs() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(ClientInfoUtil::getOs)
                .orElse(UNKNOWN);
    }

    /**
     * 获取地理位置信息
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static String getLocation(String ip) {
        return Optional.ofNullable(ip)
                .filter(ClientInfoUtil::hasText)
                .filter(value -> !UNKNOWN.equalsIgnoreCase(value))
                .map(ClientInfoUtil::resolveLocation)
                .orElse("未知");
    }

    /**
     * 获取地理位置信息
     *
     * @return 地理位置信息
     */
    public static String getLocation() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(ClientInfoUtil::getClientIp)
                .map(ClientInfoUtil::getLocation)
                .orElse("未知");
    }

    /**
     * 从User-Agent中提取版本号
     *
     * @param userAgent User-Agent字符串
     * @param token     版本号前缀
     * @return 版本号
     */
    private static String extractVersion(String userAgent, String token) {
        return Optional.of(userAgent.indexOf(token))
                .filter(idx -> idx >= 0)
                .map(idx -> {
                    var start = idx + token.length();
                    var end = Optional.of(userAgent.indexOf(" ", start))
                            .filter(pos -> pos >= 0)
                            .orElse(userAgent.length());
                    return userAgent.substring(start, end).replace("_", ".");
                })
                .orElse("");
    }

    /**
     * 解析客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    private static String resolveClientIp(HttpServletRequest request) {
        var ip = List.of(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR")
                .stream()
                .map(request::getHeader)
                .filter(ClientInfoUtil::hasText)
                .filter(value -> !UNKNOWN.equalsIgnoreCase(value))
                .findFirst()
                .orElseGet(request::getRemoteAddr);

        return Optional.ofNullable(ip)
                .map(value -> value.contains(",") ? value.split(",")[0].trim() : value)
                .filter(ClientInfoUtil::hasText)
                .orElse(UNKNOWN);
    }

    /**
     * 解析浏览器信息
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器信息
     */
    private static String parseBrowser(String userAgent) {
        Matcher matcher = BROWSER_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            var browser = matcher.group(1);
            var version = matcher.group(2);
            if (userAgent.contains("Edg")) {
                return "Edge " + extractVersion(userAgent, "Edg/");
            }
            if ("Chrome".equals(browser) && userAgent.contains("Safari") && !userAgent.contains("Chromium")) {
                return "Chrome " + version;
            }
            return browser + " " + version;
        }
        return UNKNOWN;
    }

    /**
     * 解析操作系统信息
     *
     * @param userAgent User-Agent字符串
     * @return 操作系统信息
     */
    private static String parseOs(String userAgent) {
        Matcher matcher = OS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            var os = matcher.group(1);
            if ("Windows".equals(os)) {
                if (userAgent.contains("Windows NT 10.0")) {
                    return "Windows 10";
                }
                if (userAgent.contains("Windows NT 6.3")) {
                    return "Windows 8.1";
                }
                if (userAgent.contains("Windows NT 6.2")) {
                    return "Windows 8";
                }
                if (userAgent.contains("Windows NT 6.1")) {
                    return "Windows 7";
                }
                return "Windows";
            }
            if ("Mac".equals(os) || userAgent.contains("Mac OS X")) {
                var version = extractVersion(userAgent, "Mac OS X ");
                return version.isEmpty() ? "macOS" : "macOS " + version;
            }
            if ("Linux".equals(os)) {
                return "Linux";
            }
            if ("Android".equals(os)) {
                var version = extractVersion(userAgent, "Android ");
                return "Android " + version;
            }
            if ("iOS".equals(os) || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                var version = extractVersion(userAgent, "OS ");
                return "iOS " + version;
            }
            return os;
        }
        return UNKNOWN;
    }

    /**
     * 解析地理位置信息
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    private static String resolveLocation(String ip) {
        return Optional.ofNullable(ipLocationService)
                .filter(IpLocationService::isAvailable)
                .map(service -> service.getLocation(ip))
                .filter(ClientInfoUtil::hasText)
                .orElse("未知");
    }

    /**
     * 检查字符串是否为空
     *
     * @param value 字符串
     * @return 是否为空
     */
    private static boolean hasText(String value) {
        return Optional.ofNullable(value)
                .map(text -> !text.trim().isEmpty())
                .orElse(false);
    }
}
