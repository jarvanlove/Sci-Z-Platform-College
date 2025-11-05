package com.server.agentbackendservices.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供手动检查服务连接状态的接口
 */
@Tag(name = "健康检查", description = "服务健康状态检查相关接口")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private ConnectionFactory rabbitmqConnectionFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 检查所有服务连接状态
     */
    @Operation(summary = "检查所有服务状态", description = "检查数据库、Redis、RabbitMQ等所有外部服务的连接状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成", 
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/check")
    public Map<String, Object> checkAllServices() {
        logger.info("🔍 开始检查所有服务连接状态...");
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> services = new HashMap<>();
        
        // 检查数据库
        services.put("database", checkDatabase());
        
        // 检查 Redis
        services.put("redis", checkRedis());
        
        // 检查 RabbitMQ
        services.put("rabbitmq", checkRabbitmq());
        
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "success");
        result.put("services", services);
        
        return result;
    }

    /**
     * 检查数据库连接
     */
    @Operation(summary = "检查数据库连接", description = "检查PostgreSQL数据库的连接状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "500", description = "数据库连接失败")
    })
    @GetMapping("/database")
    public Map<String, Object> checkDatabaseEndpoint() {
        return checkDatabase();
    }

    /**
     * 检查 Redis 连接
     */
    @Operation(summary = "检查Redis连接", description = "检查Redis缓存的连接状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "500", description = "Redis连接失败")
    })
    @GetMapping("/redis")
    public Map<String, Object> checkRedisEndpoint() {
        return checkRedis();
    }

    /**
     * 检查 RabbitMQ 连接
     */
    @Operation(summary = "检查RabbitMQ连接", description = "检查RabbitMQ消息队列的连接状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查完成"),
            @ApiResponse(responseCode = "500", description = "RabbitMQ连接失败")
    })
    @GetMapping("/rabbitmq")
    public Map<String, Object> checkRabbitmqEndpoint() {
        return checkRabbitmq();
    }

    /**
     * 内部方法：检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                result.put("status", "UP");
                result.put("message", "数据库连接正常");
                result.put("url", connection.getMetaData().getURL());
                result.put("database", connection.getMetaData().getDatabaseProductName());
                result.put("version", connection.getMetaData().getDatabaseProductVersion());
                result.put("driver", connection.getMetaData().getDriverName());
            } else {
                result.put("status", "DOWN");
                result.put("message", "数据库连接无效");
            }
        } catch (SQLException e) {
            result.put("status", "DOWN");
            result.put("message", "数据库连接失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 内部方法：检查 Redis 连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            redisConnectionFactory.getConnection().ping();
            result.put("status", "UP");
            result.put("message", "Redis 连接正常");
            result.put("host", redisConnectionFactory.getConnection().getNativeConnection().toString());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "Redis 连接失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    /**
     * 内部方法：检查 RabbitMQ 连接
     */
    private Map<String, Object> checkRabbitmq() {
        Map<String, Object> result = new HashMap<>();
        try {
            rabbitmqConnectionFactory.createConnection().close();
            result.put("status", "UP");
            result.put("message", "RabbitMQ 连接正常");
            result.put("host", rabbitmqConnectionFactory.getHost());
            result.put("port", rabbitmqConnectionFactory.getPort());
            result.put("virtualHost", rabbitmqConnectionFactory.getVirtualHost());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("message", "RabbitMQ 连接失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        return result;
    }
}
