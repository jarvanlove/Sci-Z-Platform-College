package com.server.agentbackendservices.modules.dify.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * Dify RestTemplate 配置类
 * 配置 HTTP 客户端的超时和连接参数
 * 
 * @author jarvanlove
 * @since 2024-10-22
 */
@Configuration
@RequiredArgsConstructor
public class DifyRestTemplateConfig {
    private final DifyConfig difyConfig;
    /**
     * 创建 RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }
    /**
     * 配置 HTTP 请求工厂
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间
        factory.setConnectTimeout(difyConfig.getConnectTimeout());
        
        // 设置读取超时时间
        factory.setReadTimeout(difyConfig.getTimeout());
        
        return factory;
    }
}
