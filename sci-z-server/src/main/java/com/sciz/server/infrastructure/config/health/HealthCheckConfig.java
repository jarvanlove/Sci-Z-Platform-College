package com.sciz.server.infrastructure.config.health;

import io.minio.MinioClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 健康检查配置类
 * 用于检查各个中间件的连接状态
 * 
 * @author JiaWen.Wu
 * @date 2025-01-XX
 */
@Configuration
public class HealthCheckConfig {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckConfig.class);

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String kafkaBootstrapServers;

    /**
     * 数据库健康检查
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    String url = connection.getMetaData().getURL();
                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
                    
                    return Health.up()
                            .withDetail("database", databaseProductName)
                            .withDetail("version", databaseProductVersion)
                            .withDetail("status", "Connected")
                            .withDetail("url", url)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connection invalid")
                            .build();
                }
            } catch (SQLException e) {
                logger.error("数据库健康检查失败", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Redis 健康检查
     */
    @Bean
    public HealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        return () -> {
            try {
                redisConnectionFactory.getConnection().ping();
                String host = redisConnectionFactory.getConnection().getNativeConnection().toString();
                
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connected")
                        .withDetail("host", host)
                        .build();
            } catch (Exception e) {
                logger.error("Redis 健康检查失败", e);
                return Health.down()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Kafka 健康检查
     */
    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            try {
                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
                props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
                
                try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
                    // 尝试发送一个测试消息到健康检查主题（如果主题不存在会失败，但不影响连接检查）
                    producer.send(new ProducerRecord<>("__health_check__", "test"));
                    producer.flush();
                }
                
                return Health.up()
                        .withDetail("message_queue", "Kafka")
                        .withDetail("status", "Connected")
                        .withDetail("bootstrap_servers", kafkaBootstrapServers)
                        .build();
            } catch (Exception e) {
                logger.error("Kafka 健康检查失败", e);
                return Health.down()
                        .withDetail("message_queue", "Kafka")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * MinIO 健康检查
     */
    @Bean
    public HealthIndicator minioHealthIndicator(MinioClient minioClient) {
        return () -> {
            try {
                // 尝试列出所有存储桶来检查连接
                minioClient.listBuckets();
                
                return Health.up()
                        .withDetail("storage", "MinIO")
                        .withDetail("status", "Connected")
                        .build();
            } catch (Exception e) {
                logger.error("MinIO 健康检查失败", e);
                return Health.down()
                        .withDetail("storage", "MinIO")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}

