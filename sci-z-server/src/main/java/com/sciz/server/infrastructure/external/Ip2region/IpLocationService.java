package com.sciz.server.infrastructure.external.Ip2region;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

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
        Optional.ofNullable(searcher).ifPresent(current -> {
            try {
                current.close();
            } catch (IOException e) {
                log.warn(String.format("关闭IP地址查询器失败: err=%s", e.getMessage()), e);
            }
        });
    }

    /**
     * 根据IP地址获取地理位置信息
     * 优先尝试通过 ip2region 解析，获取精确的地理位置信息
     * 如果解析失败或为内网IP，则返回降级值
     *
     * @param ip IP地址（IPv4或IPv6格式）
     * @return 地理位置信息，例如：上海市 松江区 (电信)
     */
    public String getLocation(String ip) {
        if (Optional.ofNullable(searcher).isEmpty()) {
            log.warn("IP地址库未初始化，返回默认值");
            return formatDefaultLocation(ip);
        }

        var normalizedIp = Optional.ofNullable(ip)
                .map(String::trim)
                .filter(value -> !value.isEmpty());

        if (normalizedIp.isEmpty()) {
            return "未知";
        }

        var actualIp = normalizedIp.get();

        // 先尝试通过 ip2region 解析（即使可能是内网IP，也尝试解析）
        try {
            var region = searcher.search(actualIp);
            if (hasText(region)) {
                var formatted = formatLocation(region);
                // 如果格式化后的结果有效（不是"未知"），则返回
                if (hasText(formatted) && !"未知".equals(formatted)) {
                    return formatted;
                }
            }
        } catch (Exception e) {
            log.debug(String.format("IP地址解析失败: ip=%s, err=%s", actualIp, e.getMessage()));
        }

        // 解析失败或返回无效值，判断是否为内网IP
        if (isInternalIp(actualIp)) {
            return "内网";
        }

        // 公网IP但解析失败，返回降级值
        return formatDefaultLocation(actualIp);
    }

    /**
     * 格式化地理位置信息
     * ip2region 返回格式：国家|区域|省份|城市|ISP
     * 转换为更友好的格式：省份 城市 (ISP)
     * 例如：上海市 松江区 (电信)
     * 
     * 注意：城市字段可能包含区县信息，如"上海市松江区"，会完整保留
     *
     * @param region ip2region 返回的原始数据
     * @return 格式化后的地理位置信息
     */
    private String formatLocation(String region) {
        return Optional.ofNullable(region)
                .filter(this::hasText)
                .map(value -> {
                    String[] parts = value.split("\\|");
                    if (parts.length < 5) {
                        return value;
                    }

                    var country = parts[0];
                    var province = parts[2];
                    var city = parts[3];
                    var isp = parts[4];

                    // 非中国地区，直接返回国家名
                    if (Optional.ofNullable(country)
                            .filter(this::hasText)
                            .filter(item -> !"0".equals(item))
                            .filter(item -> !"中国".equals(item))
                            .isPresent()) {
                        return country;
                    }

                    var location = new StringBuilder();

                    // 省份信息
                    Optional.ofNullable(province)
                            .filter(this::hasText)
                            .filter(item -> !"0".equals(item))
                            .ifPresent(location::append);

                    // 城市信息（可能包含区县，如"上海市松江区"）
                    // 如果城市与省份不同，或者城市包含区县信息，则添加城市信息
                    Optional.ofNullable(city)
                            .filter(this::hasText)
                            .filter(item -> !"0".equals(item))
                            .filter(item -> {
                                // 如果城市与省份相同（如"北京市"和"北京"），且城市不包含区县，则跳过
                                var provinceVal = Optional.ofNullable(province)
                                        .filter(this::hasText)
                                        .orElse("");
                                // 如果城市包含区县信息（长度较长或包含"区"、"县"等），则保留
                                if (item.length() > provinceVal.length() ||
                                        item.contains("区") || item.contains("县") || item.contains("市")) {
                                    return true;
                                }
                                // 如果城市与省份不同，则保留
                                return !item.equals(provinceVal);
                            })
                            .ifPresent(item -> {
                                if (location.length() > 0) {
                                    location.append(" ");
                                }
                                location.append(item);
                            });

                    // 如果没有任何位置信息，使用国家
                    if (location.length() == 0) {
                        Optional.ofNullable(country)
                                .filter(this::hasText)
                                .ifPresent(location::append);
                    }

                    // ISP 信息（运营商）
                    Optional.ofNullable(isp)
                            .filter(this::hasText)
                            .filter(item -> !"0".equals(item))
                            .ifPresent(item -> location.append(" (").append(item).append(")"));

                    return location.toString();
                })
                .orElse("未知");
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    private boolean isInternalIp(String ip) {
        return Optional.ofNullable(ip)
                .map(String::trim)
                .filter(this::hasText)
                .map(value -> {
                    var lower = value.toLowerCase(Locale.ROOT);

                    // IPv4 localhost
                    if (List.of("127.0.0.1", "localhost").contains(lower)) {
                        return true;
                    }

                    // IPv6 localhost (::1 或 0:0:0:0:0:0:0:1)
                    if (List.of("::1", "0:0:0:0:0:0:0:1", "0000:0000:0000:0000:0000:0000:0000:0001").contains(lower)) {
                        return true;
                    }

                    // IPv4 内网地址段
                    if (lower.startsWith("192.168.")
                            || lower.startsWith("10.")
                            || lower.startsWith("169.254.")) {
                        return true;
                    }
                    if (lower.startsWith("172.")) {
                        var parts = lower.split("\\.");
                        return parts.length >= 2 && parseIntSafe(parts[1])
                                .filter(num -> num >= 16 && num <= 31)
                                .isPresent();
                    }

                    // IPv6 内网地址段 (fc00::/7)
                    if (lower.startsWith("fc00:") || lower.startsWith("fd00:")) {
                        return true;
                    }

                    return false;
                })
                .orElse(false);
    }

    /**
     * 格式化默认位置信息
     *
     * @param ip IP地址
     * @return 默认位置信息
     */
    private String formatDefaultLocation(String ip) {
        return isInternalIp(ip) ? "内网"
                : String.format("IP:%s", Optional.ofNullable(ip).filter(this::hasText).orElse("未知"));
    }

    private Optional<Integer> parseIntSafe(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private boolean hasText(String value) {
        return Optional.ofNullable(value)
                .map(text -> !text.trim().isEmpty())
                .orElse(false);
    }

    /**
     * 检查IP地址库是否可用
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return Optional.ofNullable(searcher).isPresent();
    }
}
