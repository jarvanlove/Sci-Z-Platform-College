package com.sciz.server.infrastructure.shared.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sciz.server.infrastructure.external.Ip2region.IpLocationService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // 获取 IpLocationService Bean
        try {
            ClientInfoUtil.ipLocationService = applicationContext.getBean(IpLocationService.class);
        } catch (Exception e) {
            // 如果服务不存在，设置为 null
            ClientInfoUtil.ipLocationService = null;
        }
    }

    private static final String UNKNOWN = "unknown";
    private static final Pattern BROWSER_PATTERN = Pattern.compile(
            "(Chrome|Firefox|Safari|Edge|Opera|MSIE|Trident)/([\\d.]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OS_PATTERN = Pattern.compile(
            "(Windows|Mac|Linux|Android|iOS|iPhone|iPad|Unix)([\\s\\d._]*)?", Pattern.CASE_INSENSITIVE);

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip == null ? UNKNOWN : ip;
    }

    /**
     * 从当前请求上下文获取客户端IP
     *
     * @return IP地址
     */
    public static String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return getClientIp(attributes.getRequest());
        }
        return UNKNOWN;
    }

    /**
     * 获取浏览器信息
     *
     * @param request HttpServletRequest
     * @return 浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        Matcher matcher = BROWSER_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String browser = matcher.group(1);
            String version = matcher.group(2);
            // 处理 Edge 浏览器
            if (userAgent.contains("Edg")) {
                return "Edge " + extractVersion(userAgent, "Edg/");
            }
            // 处理 Chrome
            if ("Chrome".equals(browser) && userAgent.contains("Safari") && !userAgent.contains("Chromium")) {
                return "Chrome " + version;
            }
            return browser + " " + version;
        }

        return UNKNOWN;
    }

    /**
     * 从当前请求上下文获取浏览器信息
     *
     * @return 浏览器信息
     */
    public static String getBrowser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return getBrowser(attributes.getRequest());
        }
        return UNKNOWN;
    }

    /**
     * 获取操作系统信息
     *
     * @param request HttpServletRequest
     * @return 操作系统信息
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        Matcher matcher = OS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String os = matcher.group(1);
            if ("Windows".equals(os)) {
                if (userAgent.contains("Windows NT 10.0")) {
                    return "Windows 10";
                } else if (userAgent.contains("Windows NT 6.3")) {
                    return "Windows 8.1";
                } else if (userAgent.contains("Windows NT 6.2")) {
                    return "Windows 8";
                } else if (userAgent.contains("Windows NT 6.1")) {
                    return "Windows 7";
                }
                return "Windows";
            } else if ("Mac".equals(os) || userAgent.contains("Mac OS X")) {
                String version = extractVersion(userAgent, "Mac OS X ");
                return "macOS " + (version.isEmpty() ? "" : version);
            } else if ("Linux".equals(os)) {
                return "Linux";
            } else if ("Android".equals(os)) {
                String version = extractVersion(userAgent, "Android ");
                return "Android " + version;
            } else if ("iOS".equals(os) || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                String version = extractVersion(userAgent, "OS ");
                return "iOS " + version;
            }
            return os;
        }

        return UNKNOWN;
    }

    /**
     * 从当前请求上下文获取操作系统信息
     *
     * @return 操作系统信息
     */
    public static String getOs() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return getOs(attributes.getRequest());
        }
        return UNKNOWN;
    }

    /**
     * 获取登录地点（根据IP地址解析）
     * 使用 ip2region 库进行IP地址到地理位置的解析
     *
     * @param ip IP地址
     * @return 登录地点，格式：省份 城市 (ISP)，例如：北京市 北京市 (电信)
     */
    public static String getLocation(String ip) {
        if (ip == null || ip.isEmpty() || UNKNOWN.equals(ip)) {
            return "未知";
        }

        // 如果IP解析服务可用，使用它进行解析
        if (ipLocationService != null && ipLocationService.isAvailable()) {
            return ipLocationService.getLocation(ip);
        }

        // 如果服务不可用，使用简单判断
        if ("127.0.0.1".equals(ip) || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "内网";
        }

        // 172.16.x.x - 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return "内网";
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }

        return "未知";
    }

    /**
     * 从User-Agent中提取版本号
     *
     * @param userAgent User-Agent字符串
     * @param prefix    版本号前缀
     * @return 版本号
     */
    private static String extractVersion(String userAgent, String prefix) {
        int index = userAgent.indexOf(prefix);
        if (index != -1) {
            int start = index + prefix.length();
            int end = start;
            while (end < userAgent.length() && (Character.isDigit(userAgent.charAt(end)) || userAgent.charAt(end) == '.'
                    || userAgent.charAt(end) == '_')) {
                end++;
            }
            return userAgent.substring(start, end).replace('_', '.');
        }
        return "";
    }
}
