package com.server.agentbackendservices;

import com.server.agentbackendservices.modules.dify.config.DifyConfig;
import com.server.agentbackendservices.modules.dify.util.DifyApiClient;
import com.server.agentbackendservices.modules.dify.service.DifyApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dify模块测试
 */
@SpringBootTest
@ActiveProfiles("dev")
public class DifyModuleTest {

    @Autowired
    private DifyConfig difyConfig;

    @Autowired
    private DifyApiClient difyApiClient;

    @Autowired
    private DifyApiService difyApiService;

    @Test
    public void testDifyConfig() {
        assertNotNull(difyConfig);
        assertNotNull(difyConfig.getBaseUrl());
        assertNotNull(difyConfig.getApiKey());
        System.out.println("Dify配置加载成功:");
        System.out.println("Base URL: " + difyConfig.getBaseUrl());
        System.out.println("API Key: " + difyConfig.getApiKey());
    }

    @Test
    public void testDifyApiClient() {
        assertNotNull(difyApiClient);
        System.out.println("DifyApiClient注入成功");
    }

    @Test
    public void testDifyApiService() {
        assertNotNull(difyApiService);
        System.out.println("DifyApiService注入成功");
    }
}
