package com.server.agentbackendservices.modules.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 健康检查配置类
 * 用于检查各个服务的连接状态
 */
@Configuration
public class HealthCheckConfig {

    /**
     * 数据库健康检查
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connected")
                            .withDetail("url", connection.getMetaData().getURL())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connection invalid")
                            .build();
                }
            } catch (SQLException e) {
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
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connected")
                        .withDetail("host", redisConnectionFactory.getConnection().getNativeConnection().toString())
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * RabbitMQ 健康检查
     */
    @Bean
    public HealthIndicator rabbitmqHealthIndicator(ConnectionFactory connectionFactory) {
        return () -> {
            try {
                connectionFactory.createConnection().close();
                return Health.up()
                        .withDetail("message_queue", "RabbitMQ")
                        .withDetail("status", "Connected")
                        .withDetail("host", connectionFactory.getHost())
                        .withDetail("port", connectionFactory.getPort())
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("message_queue", "RabbitMQ")
                        .withDetail("status", "Connection failed")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}
