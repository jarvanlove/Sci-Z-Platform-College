package com.sciz.server.infrastructure.config.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Dify 配置：集中管理 API 地址、密钥、超时等；提供 WebClient 供集成实现使用。
 * 
 * @author JiaWen.Wu
 * @className DifyConfig
 * @date 2025-10-29 10:00
 */
@Configuration
@EnableConfigurationProperties(DifyConfig.DifyProps.class)
public class DifyConfig {

    @Bean("difyRestTemplate")
    public RestTemplate difyRestTemplate(DifyProps props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getHttp().getConnectTimeoutMs());
        factory.setReadTimeout(props.getHttp().getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    @ConfigurationProperties(prefix = "dify")
    public static class DifyProps {
        private Api api = new Api();
        private Http http = new Http();

        public Api getApi() {
            return api;
        }

        public void setApi(Api api) {
            this.api = api;
        }

        public Http getHttp() {
            return http;
        }

        public void setHttp(Http http) {
            this.http = http;
        }

        public static class Api {
            private String baseUrl = "https://api.dify.ai/v1";
            private String apiKey = "";

            public String getBaseUrl() {
                return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }
        }

        public static class Http {
            private int connectTimeoutMs = 5000;
            private int readTimeoutMs = 60000;

            public int getConnectTimeoutMs() {
                return connectTimeoutMs;
            }

            public void setConnectTimeoutMs(int connectTimeoutMs) {
                this.connectTimeoutMs = connectTimeoutMs;
            }

            public int getReadTimeoutMs() {
                return readTimeoutMs;
            }

            public void setReadTimeoutMs(int readTimeoutMs) {
                this.readTimeoutMs = readTimeoutMs;
            }
        }
    }
}
