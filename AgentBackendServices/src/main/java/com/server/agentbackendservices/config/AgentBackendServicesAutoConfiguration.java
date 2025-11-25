package com.server.agentbackendservices.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * AgentBackendServices 自动配置类
 * 
 * 当其他项目引入此依赖时，会自动扫描并注册以下组件：
 * - Service 层：DifyApiService、DifyApiKeyService、AuthService、UserService 等
 * - Config 层：DifyConfig、DifyDocumentConfig、SaTokenConfig 等
 * - Util 层：DifyApiClient、TokenUtils 等
 * - Mapper 层：自动扫描所有 Mapper 接口
 *
 * @author AgentBackendServices
 * @className AgentBackendServicesAutoConfiguration
 * @date 2025-01-24 10:00
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.server.agentbackendservices.modules.dify",
    "com.server.agentbackendservices.modules.auth",
    "com.server.agentbackendservices.modules.common",
    "com.server.agentbackendservices.modules.config"
})
@MapperScan(basePackages = "com.server.agentbackendservices.modules.**.mapper")
public class AgentBackendServicesAutoConfiguration {
    
    /**
     * 自动配置类，无需额外配置
     * Spring Boot 会自动加载此配置类
     */
}


