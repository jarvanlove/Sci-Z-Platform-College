package com.sciz.server.infrastructure.external.Ip2region;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.InputStream;

/**
 * IP地址解析服务
 * 使用 ip2region 库进行IP地址到地理位置的解析
 *
 * @author JiaWen.Wu
 * @className IpLocationService
 * @date 2025-01-27 15:30
 */
@Slf4j
@Component
public class IpLocationService {

    private Searcher searcher;
    private byte[] contentBuff;

    /**
     * 初始化IP地址库
     */
    @PostConstruct
    public void init() {
        try {
            // 从 classpath 加载 ip2region/ip2region.xdb 文件
            ClassPathResource resource = new ClassPathResource("ip2region/ip2region.xdb");
            if (!resource.exists()) {
                log.warn(String.format("IP地址库文件不存在，IP地址解析功能将不可用。请下载到: %s",
                        "src/main/resources/ip2region/ip2region.xdb"));
                return;
            }

            // 读取数据文件到内存
            try (InputStream inputStream = resource.getInputStream()) {
                contentBuff = inputStream.readAllBytes();
            }

            // 创建查询器（使用内存模式，性能最佳）
            // ip2region 2.7.0 版本：使用 newWithBuffer 方法，将整个数据文件加载到内存
            searcher = Searcher.newWithBuffer(contentBuff);

            log.info(String.format("IP地址库初始化成功，文件大小: %s KB，查询性能: 微秒级", contentBuff.length / 1024));

        } catch (Exception e) {
            log.error(String.format("IP地址库初始化失败，将使用默认的IP解析功能: err=%s", e.getMessage()), e);
            searcher = null;
        }
    }

    /**
     * 释放资源
     */
    @PreDestroy
    public void destroy() {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException e) {
                log.warn(String.format("关闭IP地址查询器失败: err=%s", e.getMessage()), e);
            }
        }
    }

    /**
     * 根据IP地址获取地理位置信息
     *
     * @param ip IP地址（IPv4格式）
     * @return 地理位置信息，格式：国家|区域|省份|城市|ISP
     *         例如：中国|0|北京|北京市|电信
     */
    public String getLocation(String ip) {
        if (searcher == null) {
            log.warn(String.format("IP地址库未初始化，返回默认值"));
            return formatDefaultLocation(ip);
        }

        if (ip == null || ip.isEmpty()) {
            return "未知";
        }

        // 处理内网IP
        if (isInternalIp(ip)) {
            return "内网";
        }

        try {
            // 使用 ip2region 查询
            String region = searcher.search(ip);
            if (region == null || region.isEmpty()) {
                return "未知";
            }

            // 格式化返回结果
            return formatLocation(region);

        } catch (Exception e) {
            log.warn(String.format("IP地址解析失败: ip=%s, err=%s", ip, e.getMessage()));
            return formatDefaultLocation(ip);
        }
    }

    /**
     * 格式化地理位置信息
     * ip2region 返回格式：国家|区域|省份|城市|ISP
     * 转换为更友好的格式：省份 城市 (ISP)
     * 例如：北京市 北京市 (电信)
     *
     * @param region ip2region 返回的原始数据
     * @return 格式化后的地理位置信息
     */
    private String formatLocation(String region) {
        if (region == null || region.isEmpty()) {
            return "未知";
        }

        String[] parts = region.split("\\|");
        if (parts.length < 5) {
            return region;
        }

        String country = parts[0];
        String province = parts[2];
        String city = parts[3];
        String isp = parts[4];

        // 如果国家不是中国，返回国家信息
        if (!"中国".equals(country) && !"0".equals(country) && !country.isEmpty()) {
            return country;
        }

        // 格式化省份和城市
        StringBuilder location = new StringBuilder();
        if (province != null && !province.isEmpty() && !"0".equals(province)) {
            location.append(province);
        }
        if (city != null && !city.isEmpty() && !"0".equals(city) && province != null && !province.equals(city)) {
            if (location.length() > 0) {
                location.append(" ");
            }
            location.append(city);
        }

        // 如果省份和城市都为空，返回国家
        if (location.length() == 0) {
            location.append(country);
        }

        // 添加ISP信息（如果有）
        if (isp != null && !isp.isEmpty() && !"0".equals(isp)) {
            location.append(" (").append(isp).append(")");
        }

        return location.toString();
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    private boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 127.0.0.1
        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return true;
        }

        // 192.168.x.x
        if (ip.startsWith("192.168.")) {
            return true;
        }

        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }

        // 172.16.x.x - 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }

        // 169.254.x.x (链路本地地址)
        if (ip.startsWith("169.254.")) {
            return true;
        }

        return false;
    }

    /**
     * 格式化默认位置信息
     *
     * @param ip IP地址
     * @return 默认位置信息
     */
    private String formatDefaultLocation(String ip) {
        if (isInternalIp(ip)) {
            return "内网";
        }
        return "未知";
    }

    /**
     * 检查IP地址库是否可用
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return searcher != null;
    }
}
