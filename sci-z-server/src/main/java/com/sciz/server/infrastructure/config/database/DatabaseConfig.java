package com.sciz.server.infrastructure.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据库连接池配置
 * 
 * @author JiaWen.Wu
 * @className DatabaseConfig
 * @date 2025-10-29 17:00
 */
@Configuration
public class DatabaseConfig {

    /**
     * 绑定 spring.datasource.* 到 DataSourceProperties
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * HikariCP 参数绑定：spring.datasource.hikari.*
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    /**
     * DataSource Bean（HikariDataSource）
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties, HikariConfig hikariConfig) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        // 覆盖/补充 Hikari 参数
        dataSource.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
        dataSource.setMinimumIdle(hikariConfig.getMinimumIdle());
        dataSource.setIdleTimeout(hikariConfig.getIdleTimeout());
        dataSource.setMaxLifetime(hikariConfig.getMaxLifetime());
        dataSource.setConnectionTimeout(hikariConfig.getConnectionTimeout());
        dataSource.setPoolName(hikariConfig.getPoolName());
        return dataSource;
    }
}
