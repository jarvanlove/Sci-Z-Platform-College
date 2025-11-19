package com.sciz.server.infrastructure.config.health;

import io.minio.MinioClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 启动时健康检查监听器
 * 在应用启动完成后检查所有中间件的连接状态
 * 
 * @author JiaWen.Wu
 * @date 2025-01-XX
 */
@Component
public class StartupHealthCheckListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartupHealthCheckListener.class);

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private MinioClient minioClient;

    @Autowired
    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("==========================================");
        logger.info("🔍 开始启动时中间件健康检查...");
        logger.info("==========================================");

        Map<String, HealthCheckResult> results = new HashMap<>();
        List<String> failedServices = new ArrayList<>();

        // 检查数据库
        HealthCheckResult dbResult = checkDatabase();
        results.put("数据库 (PostgreSQL)", dbResult);
        if (!dbResult.isHealthy()) {
            failedServices.add("数据库");
        }

        // 检查 Redis
        if (redisConnectionFactory != null) {
            HealthCheckResult redisResult = checkRedis();
            results.put("Redis", redisResult);
            if (!redisResult.isHealthy()) {
                failedServices.add("Redis");
            }
        } else {
            logger.warn("⚠️  Redis 连接工厂未配置，跳过检查");
        }

        // 检查 Kafka
        HealthCheckResult kafkaResult = checkKafka();
        results.put("Kafka", kafkaResult);
        if (!kafkaResult.isHealthy()) {
            failedServices.add("Kafka");
        }

        // 检查 MinIO
        if (minioClient != null) {
            HealthCheckResult minioResult = checkMinIO();
            results.put("MinIO", minioResult);
            if (!minioResult.isHealthy()) {
                failedServices.add("MinIO");
            }
        } else {
            logger.warn("⚠️  MinIO 客户端未配置，跳过检查");
        }

        // 输出检查结果
        logger.info("==========================================");
        logger.info("📊 健康检查结果汇总：");
        logger.info("==========================================");
        
        results.forEach((service, result) -> {
            if (result.isHealthy()) {
                logger.info("✅ {} - 正常", service);
                if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                    result.getDetails().forEach((key, value) -> 
                        logger.info("   {}: {}", key, value));
                }
            } else {
                logger.error("❌ {} - 异常: {}", service, result.getErrorMessage());
            }
        });

        logger.info("==========================================");

        if (!failedServices.isEmpty()) {
            logger.error("⚠️  以下中间件连接失败: {}", String.join(", ", failedServices));
            logger.error("⚠️  应用已启动，但部分功能可能不可用，请检查配置");
        } else {
            logger.info("✅ 所有中间件连接正常，应用启动成功！");
        }

        logger.info("==========================================");
    }

    /**
     * 检查数据库连接
     */
    private HealthCheckResult checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                Map<String, Object> details = new HashMap<>();
                details.put("数据库类型", connection.getMetaData().getDatabaseProductName());
                details.put("版本", connection.getMetaData().getDatabaseProductVersion());
                details.put("URL", connection.getMetaData().getURL());
                return HealthCheckResult.success(details);
            } else {
                return HealthCheckResult.failure("数据库连接无效");
            }
        } catch (SQLException e) {
            logger.error("数据库连接检查失败", e);
            return HealthCheckResult.failure("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查 Redis 连接
     */
    private HealthCheckResult checkRedis() {
        try {
            redisConnectionFactory.getConnection().ping();
            Map<String, Object> details = new HashMap<>();
            details.put("状态", "已连接");
            return HealthCheckResult.success(details);
        } catch (Exception e) {
            logger.error("Redis 连接检查失败", e);
            return HealthCheckResult.failure("Redis 连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查 Kafka 连接
     * 使用较短的超时时间，避免启动时长时间等待
     */
    private HealthCheckResult checkKafka() {
        try {
            String bootstrapServers = environment.getProperty("kafka.bootstrap-servers", "localhost:9092");
            
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            
            // 缩短超时时间，快速失败
            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000); // 请求超时 2 秒
            props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 5000); // 连接最大空闲时间 5 秒
            props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 10000); // 元数据最大年龄 10 秒
            
            // 减少重连和重试时间
            props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 50); // 重连退避时间 50ms
            props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // 重试退避时间 100ms
            
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
                producer.send(new ProducerRecord<>("__health_check__", "test"));
                producer.flush();
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("Bootstrap Servers", bootstrapServers);
            details.put("状态", "已连接");
            return HealthCheckResult.success(details);
        } catch (Exception e) {
            logger.warn("Kafka 连接检查失败: {}", e.getMessage());
            return HealthCheckResult.failure("Kafka 连接失败: " + e.getMessage());
        }
    }

    /**
     * 检查 MinIO 连接
     */
    private HealthCheckResult checkMinIO() {
        try {
            minioClient.listBuckets();
            Map<String, Object> details = new HashMap<>();
            details.put("状态", "已连接");
            return HealthCheckResult.success(details);
        } catch (Exception e) {
            logger.error("MinIO 连接检查失败", e);
            return HealthCheckResult.failure("MinIO 连接失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查结果内部类
     */
    private static class HealthCheckResult {
        private final boolean healthy;
        private final String errorMessage;
        private final Map<String, Object> details;

        private HealthCheckResult(boolean healthy, String errorMessage, Map<String, Object> details) {
            this.healthy = healthy;
            this.errorMessage = errorMessage;
            this.details = details != null ? details : new HashMap<>();
        }

        public static HealthCheckResult success(Map<String, Object> details) {
            return new HealthCheckResult(true, null, details);
        }

        public static HealthCheckResult failure(String errorMessage) {
            return new HealthCheckResult(false, errorMessage, null);
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }
}

