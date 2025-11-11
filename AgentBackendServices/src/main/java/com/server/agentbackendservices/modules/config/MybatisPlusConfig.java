//package com.server.agentbackendservices.config;
//
//import com.baomidou.mybatisplus.annotation.DbType;
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * MyBatis Plus 配置类
// *
// * @author AgentBackendServices
// * @since 2024-01-01
// */
//@Configuration
//public class MybatisPlusConfig {
//
//    /**
//     * 分页插件配置
//     * 支持多种数据库的分页查询
//     */
//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//
//        // 分页插件
//        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
//        // 设置数据库类型为PostgreSQL
//        paginationInnerInterceptor.setDbType(DbType.POSTGRE_SQL);
//        // 设置最大单页限制数量，默认 500 条，-1 不受限制
//        paginationInnerInterceptor.setMaxLimit(1000L);
//        // 溢出总页数后是否进行处理
//        paginationInnerInterceptor.setOverflow(false);
//
//        interceptor.addInnerInterceptor(paginationInnerInterceptor);
//
//        return interceptor;
//    }
//}
