package com.sciz.server.infrastructure.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author JiaWen.Wu
 * @className SwaggerConfig
 * @date 2025-10-28 00:00
 */
@Configuration
public class SwaggerConfig {

        /**
         * OpenAPI 基础配置
         *
         * @param
         * @return OpenAPI 文档对象
         */
        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("高校科研项目管理平台 API")
                                                .version("v1.0.0")
                                                .description("科研项目管理系统API文档")
                                                .contact(new Contact().name("Sci-Z Platform Team")
                                                                .email("jarvanlovehhz@gmail.com")))
                                .externalDocs(new ExternalDocumentation().description("项目文档")
                                                .url("https://docs.sciz.edu.cn"))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                                .components(new Components().addSecuritySchemes("Bearer Authentication",
                                                new SecurityScheme().name("Authorization")
                                                                .type(SecurityScheme.Type.HTTP).scheme("bearer")
                                                                .bearerFormat("JWT")));
        }
}
