package com.server.agentbackendservices.modules.config;//package com.server.agentbackendservices.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.actuate.health.Health;
//import org.springframework.boot.actuate.health.HealthIndicator;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 启动健康检查监听器
// * 在应用启动完成后检查所有外部服务的连接状态
// */
//@Component
//public class StartupHealthCheckListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(StartupHealthCheckListener.class);
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;
//
//    @Autowired
//    private ConnectionFactory rabbitmqConnectionFactory;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    /**
//     * 应用启动完成后执行健康检查
//     */
//    @EventListener(ApplicationReadyEvent.class)
//    public void onApplicationReady() {
//        logger.info("🚀 应用启动完成，开始执行服务连接检查...");
//
//        List<ServiceCheckResult> results = new ArrayList<>();
//
//        // 检查数据库连接
//        results.add(checkDatabaseConnection());
//
//        // 检查 Redis 连接
//        results.add(checkRedisConnection());
//
//        // 检查 RabbitMQ 连接
//        results.add(checkRabbitmqConnection());
//
//        // 输出检查结果
//        printHealthCheckResults(results);
//    }
//
//    /**
//     * 检查数据库连接
//     */
//    private ServiceCheckResult checkDatabaseConnection() {
//        try {
//            try (Connection connection = dataSource.getConnection()) {
//                if (connection.isValid(5)) {
//                    String url = connection.getMetaData().getURL();
//                    String database = connection.getMetaData().getDatabaseProductName();
//                    String version = connection.getMetaData().getDatabaseProductVersion();
//
//                    logger.info("✅ 数据库连接正常: {} {}", database, version);
//                    return new ServiceCheckResult("PostgreSQL", true,
//                        String.format("Connected to %s at %s", database, url));
//                } else {
//                    logger.error("❌ 数据库连接无效");
//                    return new ServiceCheckResult("PostgreSQL", false, "Connection invalid");
//                }
//            }
//        } catch (SQLException e) {
//            logger.error("❌ 数据库连接失败: {}", e.getMessage());
//            return new ServiceCheckResult("PostgreSQL", false, "Connection failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 检查 Redis 连接
//     */
//    private ServiceCheckResult checkRedisConnection() {
//        try {
//            redisConnectionFactory.getConnection().ping();
//            String host = redisConnectionFactory.getConnection().getNativeConnection().toString();
//            logger.info("✅ Redis 连接正常: {}", host);
//            return new ServiceCheckResult("Redis", true, "Connected to " + host);
//        } catch (Exception e) {
//            logger.error("❌ Redis 连接失败: {}", e.getMessage());
//            return new ServiceCheckResult("Redis", false, "Connection failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 检查 RabbitMQ 连接
//     */
//    private ServiceCheckResult checkRabbitmqConnection() {
//        try {
//            rabbitmqConnectionFactory.createConnection().close();
//            String host = rabbitmqConnectionFactory.getHost();
//            int port = rabbitmqConnectionFactory.getPort();
//            logger.info("✅ RabbitMQ 连接正常: {}:{}", host, port);
//            return new ServiceCheckResult("RabbitMQ", true,
//                String.format("Connected to %s:%d", host, port));
//        } catch (Exception e) {
//            logger.error("❌ RabbitMQ 连接失败: {}", e.getMessage());
//            return new ServiceCheckResult("RabbitMQ", false, "Connection failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 打印健康检查结果
//     */
//    private void printHealthCheckResults(List<ServiceCheckResult> results) {
//        logger.info("=".repeat(60));
//        logger.info("📊 服务连接状态检查报告");
//        logger.info("=".repeat(60));
//
//        int successCount = 0;
//        int totalCount = results.size();
//
//        for (ServiceCheckResult result : results) {
//            String status = result.isSuccess() ? "✅ 正常" : "❌ 失败";
//            logger.info("{} {} - {}", status, result.getServiceName(), result.getMessage());
//            if (result.isSuccess()) {
//                successCount++;
//            }
//        }
//
//        logger.info("-".repeat(60));
//        logger.info("📈 检查结果: {}/{} 服务连接正常", successCount, totalCount);
//
//        if (successCount == totalCount) {
//            logger.info("🎉 所有服务连接正常，应用可以正常使用！");
//        } else {
//            logger.warn("⚠️  部分服务连接失败，请检查配置和网络连接");
//        }
//
//        logger.info("=".repeat(60));
//    }
//
//    /**
//     * 服务检查结果内部类
//     */
//    private static class ServiceCheckResult {
//        private final String serviceName;
//        private final boolean success;
//        private final String message;
//
//        public ServiceCheckResult(String serviceName, boolean success, String message) {
//            this.serviceName = serviceName;
//            this.success = success;
//            this.message = message;
//        }
//
//        public String getServiceName() {
//            return serviceName;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//    }
//}
