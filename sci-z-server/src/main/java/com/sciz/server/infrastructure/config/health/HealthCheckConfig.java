package com.sciz.server.infrastructure.config.health;

import com.sciz.server.infrastructure.config.mail.MailProviderProperties;
import com.sciz.server.infrastructure.external.mail.provider.MailProviderRegistry;
import com.sciz.server.infrastructure.shared.enums.MailProviderType;
import com.sciz.server.infrastructure.shared.utils.DateUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 健康检查配置类
 * 
 * 基于 Spring Boot Actuator 的 HealthIndicator 实现，提供标准的健康检查端点
 * 适用于生产环境，可被负载均衡器（Nginx、HAProxy）、监控系统（Prometheus、Zabbix）调用
 * 
 * 访问端点：
 * - /actuator/health - 总体健康状态
 * - /actuator/health/{component} - 单个组件健康状态（如 /actuator/health/database）
 * 
 * @author JiaWen.Wu
 * @className HealthCheckConfig
 * @date 2025-11-14 10:00
 */
@Slf4j
@Configuration
public class HealthCheckConfig {

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String kafkaBootstrapServers;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    /**
     * 数据库健康检查
     * 
     * 条件：只有当 management.health.db.enabled=true 时才创建此 Bean
     *
     * @param dataSource 数据源
     * @return 健康检查指示器
     */
    @Bean
    @ConditionalOnProperty(name = "management.health.db.enabled", havingValue = "true", matchIfMissing = true)
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            var startTime = DateUtil.now();
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    var metaData = connection.getMetaData();
                    var databaseProductName = metaData.getDatabaseProductName();
                    var databaseProductVersion = metaData.getDatabaseProductVersion();
                    var url = metaData.getURL();
                    var driverName = metaData.getDriverName();
                    var driverVersion = metaData.getDriverVersion();
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());

                    log.info(String.format(
                            "数据库健康检查成功: database=%s, version=%s, driver=%s, driverVersion=%s, url=%s, responseTime=%dms",
                            databaseProductName, databaseProductVersion, driverName, driverVersion, url, responseTime));
                    return Health.up()
                            .withDetail("database", databaseProductName)
                            .withDetail("version", databaseProductVersion)
                            .withDetail("status", "Connected")
                            .withDetail("url", url)
                            .withDetail("driver", driverName)
                            .withDetail("driverVersion", driverVersion)
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                } else {
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                    log.warn(String.format(
                            "数据库健康检查失败: status=Connection invalid, responseTime=%dms, reason=连接验证失败（isValid返回false）",
                            responseTime));
                    return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connection invalid")
                            .withDetail("responseTime", responseTime + "ms")
                            .withDetail("reason", "连接验证失败（isValid返回false）")
                            .build();
                }
            } catch (SQLException e) {
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.error(String.format(
                        "数据库健康检查失败: err=%s, errorCode=%s, sqlState=%s, responseTime=%dms, cause=%s",
                        e.getMessage(), e.getErrorCode(), e.getSQLState(), responseTime, e.getClass().getSimpleName()),
                        e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .withDetail("errorCode", e.getErrorCode())
                        .withDetail("sqlState", e.getSQLState())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }
        };
    }

    /**
     * Redis 健康检查
     * 
     * 条件：只有当 management.health.redis.enabled=true 时才创建此 Bean
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return 健康检查指示器
     */
    @Bean
    @ConditionalOnProperty(name = "management.health.redis.enabled", havingValue = "true", matchIfMissing = true)
    public HealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        return () -> {
            var startTime = DateUtil.now();
            try {
                var connection = redisConnectionFactory.getConnection();
                var pingResult = connection.ping();
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                connection.close();

                log.info(String.format(
                        "Redis 健康检查成功: status=Connected, pingResult=%s, responseTime=%dms, connectionClass=%s",
                        pingResult, responseTime, connection.getClass().getSimpleName()));
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connected")
                        .withDetail("pingResult", pingResult)
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            } catch (Exception e) {
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.error(String.format(
                        "Redis 健康检查失败: err=%s, errorType=%s, responseTime=%dms, cause=%s",
                        e.getMessage(), e.getClass().getSimpleName(), responseTime, e.getCause() != null
                                ? e.getCause().getClass().getSimpleName()
                                : "null"),
                        e);
                return Health.down()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .withDetail("errorType", e.getClass().getSimpleName())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }
        };
    }

    /**
     * Kafka 健康检查
     * 
     * 注意：仅检查连接性，不实际发送消息（避免创建临时主题）
     * 
     * 条件：只有当 management.health.kafka.enabled=true 时才创建此 Bean
     *
     * @return 健康检查指示器
     */
    @Bean
    @ConditionalOnProperty(name = "management.health.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            var startTime = DateUtil.now();
            try {
                var props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
                props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 5000);

                // 仅创建 Producer 来验证连接，不实际发送消息
                try (var producer = new KafkaProducer<String, String>(props)) {
                    // 通过获取元数据来验证连接（不发送消息）
                    var partitions = producer.partitionsFor("__health_check_topic__");
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());

                    log.info(String.format(
                            "Kafka 健康检查成功: bootstrap_servers=%s, status=Connected, responseTime=%dms, partitionsInfo=%s",
                            kafkaBootstrapServers, responseTime,
                            partitions != null ? "available" : "topic_not_found"));
                    return Health.up()
                            .withDetail("message_queue", "Kafka")
                            .withDetail("status", "Connected")
                            .withDetail("bootstrap_servers", kafkaBootstrapServers)
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                }
            } catch (Exception e) {
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.error(String.format(
                        "Kafka 健康检查失败: bootstrap_servers=%s, err=%s, errorType=%s, responseTime=%dms, cause=%s",
                        kafkaBootstrapServers, e.getMessage(), e.getClass().getSimpleName(), responseTime,
                        e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null"),
                        e);
                return Health.down()
                        .withDetail("message_queue", "Kafka")
                        .withDetail("status", "Connection failed")
                        .withDetail("bootstrap_servers", kafkaBootstrapServers)
                        .withDetail("error", e.getMessage())
                        .withDetail("errorType", e.getClass().getSimpleName())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }
        };
    }

    /**
     * MinIO 健康检查
     * 
     * 条件：只有当 management.health.minio.enabled=true 时才创建此 Bean
     *
     * @param minioClient MinIO 客户端
     * @return 健康检查指示器
     */
    @Bean
    @ConditionalOnProperty(name = "management.health.minio.enabled", havingValue = "true", matchIfMissing = false)
    public HealthIndicator minioHealthIndicator(MinioClient minioClient) {
        return () -> {
            var startTime = DateUtil.now();
            try {
                // 尝试列出所有存储桶来检查连接
                var buckets = minioClient.listBuckets();
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                var bucketCount = buckets != null ? buckets.size() : 0;

                log.info(String.format(
                        "MinIO 健康检查成功: status=Connected, bucketCount=%d, responseTime=%dms, endpoint=%s",
                        bucketCount, responseTime, minioEndpoint));
                return Health.up()
                        .withDetail("storage", "MinIO")
                        .withDetail("status", "Connected")
                        .withDetail("bucketCount", bucketCount)
                        .withDetail("endpoint", minioEndpoint)
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            } catch (Exception e) {
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.error(String.format(
                        "MinIO 健康检查失败: endpoint=%s, err=%s, errorType=%s, responseTime=%dms, cause=%s",
                        minioEndpoint, e.getMessage(), e.getClass().getSimpleName(), responseTime,
                        e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null"),
                        e);
                return Health.down()
                        .withDetail("storage", "MinIO")
                        .withDetail("status", "Connection failed")
                        .withDetail("endpoint", minioEndpoint)
                        .withDetail("error", e.getMessage())
                        .withDetail("errorType", e.getClass().getSimpleName())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }
        };
    }

    /**
     * 邮箱健康检查
     * 
     * 检查已启用的邮箱服务商连接状态
     * 条件：只有当 management.health.mail.enabled=true 时才创建此 Bean
     *
     * @param mailProviderRegistry   邮箱服务商注册表（可选，如果未配置邮箱则不会注入）
     * @param mailProviderProperties 邮箱配置属性（可选，如果未配置邮箱则不会注入）
     * @return 健康检查指示器
     */
    @Bean
    @ConditionalOnProperty(name = "management.health.mail.enabled", havingValue = "true", matchIfMissing = false)
    public HealthIndicator mailHealthIndicator(
            @Autowired(required = false) MailProviderRegistry mailProviderRegistry,
            @Autowired(required = false) MailProviderProperties mailProviderProperties) {
        return () -> {
            var startTime = DateUtil.now();
            try {
                // 如果没有配置邮箱服务，返回未配置状态
                if (mailProviderProperties == null || mailProviderProperties.providers() == null
                        || mailProviderProperties.providers().isEmpty()) {
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                    log.warn(String.format(
                            "邮箱健康检查警告: status=Not configured, reason=未配置任何邮箱服务商, responseTime=%dms",
                            responseTime));
                    return Health.unknown()
                            .withDetail("mail", "No mail providers configured")
                            .withDetail("status", "Not configured")
                            .withDetail("reason", "未配置任何邮箱服务商")
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                }

                // 检查已启用的邮箱服务商
                Map<MailProviderType, MailProviderProperties.ProviderConfig> providers = mailProviderProperties
                        .providers();
                List<String> enabledProviders = providers.entrySet().stream()
                        .filter(entry -> entry.getValue().enabled())
                        .map(entry -> entry.getKey().name())
                        .toList();

                if (enabledProviders.isEmpty()) {
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                    log.warn(String.format(
                            "邮箱健康检查警告: status=No providers enabled, reason=所有邮箱服务商均未启用, totalProviders=%d, responseTime=%dms",
                            providers.size(), responseTime));
                    return Health.unknown()
                            .withDetail("mail", "No enabled mail providers")
                            .withDetail("status", "No providers enabled")
                            .withDetail("totalProviders", providers.size())
                            .withDetail("reason", "所有邮箱服务商均未启用")
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                }

                // 尝试检查第一个已启用的服务商（简化实现，实际可以检查所有服务商）
                var firstEnabled = providers.entrySet().stream()
                        .filter(entry -> entry.getValue().enabled())
                        .findFirst();

                if (firstEnabled.isPresent()) {
                    var providerType = firstEnabled.get().getKey();
                    var config = firstEnabled.get().getValue();

                    // 检查配置是否有效
                    boolean configValid = config.smtpEnabled() || config.oauthEnabled();
                    if (!configValid) {
                        var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                        log.warn(String.format(
                                "邮箱健康检查失败: provider=%s, status=Configuration invalid, reason=SMTP和OAuth配置均无效, smtpEnabled=%s, oauthEnabled=%s, responseTime=%dms",
                                providerType.name(), config.smtpEnabled(), config.oauthEnabled(), responseTime));
                        return Health.down()
                                .withDetail("mail", "Mail provider configuration invalid")
                                .withDetail("provider", providerType.name())
                                .withDetail("status", "Configuration invalid")
                                .withDetail("reason", "SMTP和OAuth配置均无效")
                                .withDetail("smtpEnabled", config.smtpEnabled())
                                .withDetail("oauthEnabled", config.oauthEnabled())
                                .withDetail("responseTime", responseTime + "ms")
                                .build();
                    }

                    // 如果配置了注册表，可以进一步检查连接（这里简化处理，仅检查配置有效性）
                    var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                    var configType = config.smtpEnabled() ? "SMTP" : "OAuth";
                    log.info(String.format(
                            "邮箱健康检查成功: enabled_providers=%s, checkedProvider=%s, configType=%s, totalEnabled=%d, responseTime=%dms",
                            enabledProviders, providerType.name(), configType, enabledProviders.size(), responseTime));
                    return Health.up()
                            .withDetail("mail", "Mail providers configured")
                            .withDetail("enabled_providers", enabledProviders)
                            .withDetail("checkedProvider", providerType.name())
                            .withDetail("configType", configType)
                            .withDetail("totalEnabled", enabledProviders.size())
                            .withDetail("status", "Configured")
                            .withDetail("responseTime", responseTime + "ms")
                            .build();
                }

                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.warn(String.format(
                        "邮箱健康检查警告: status=Unknown, reason=未找到已启用的服务商, totalProviders=%d, responseTime=%dms",
                        providers.size(), responseTime));
                return Health.unknown()
                        .withDetail("mail", "Mail providers status unknown")
                        .withDetail("status", "Unknown")
                        .withDetail("reason", "未找到已启用的服务商")
                        .withDetail("totalProviders", providers.size())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            } catch (Exception e) {
                var responseTime = DateUtil.millisBetween(startTime, DateUtil.now());
                log.error(String.format(
                        "邮箱健康检查失败: err=%s, errorType=%s, responseTime=%dms, cause=%s",
                        e.getMessage(), e.getClass().getSimpleName(), responseTime,
                        e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null"),
                        e);
                return Health.down()
                        .withDetail("mail", "Mail health check failed")
                        .withDetail("status", "Check failed")
                        .withDetail("error", e.getMessage())
                        .withDetail("errorType", e.getClass().getSimpleName())
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }
        };
    }
}
