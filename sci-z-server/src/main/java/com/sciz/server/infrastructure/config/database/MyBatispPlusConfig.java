package com.sciz.server.infrastructure.config.database;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;

/**
 * @author JiaWen.Wu
 * @className MyBatispPlusConfig
 * @date 2025-10-29 10:00
 */
@Configuration
@MapperScan("com.sciz.server.domain.pojo.mapper")
public class MyBatispPlusConfig {

    /**
     * MyBatis-Plus 拦截器：分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
