package com.sciz.server.infrastructure.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用 Web 层配置
 * 
 * - 配置全局 CORS 规则
 * - 配置静态资源与 Swagger 资源映射
 *
 * @author JiaWen.Wu
 * @className WebConfig
 * @date 2025-10-29 15:30
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局跨域规则
     *
     * @param registry CorsRegistry CORS 注册器
     * @return void
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源与 Swagger 资源映射
     *
     * @param registry ResourceHandlerRegistry 资源注册器
     * @return void
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源 - 只处理 /static/** 路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Swagger UI (Springdoc)
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-ui/");
    }
}
