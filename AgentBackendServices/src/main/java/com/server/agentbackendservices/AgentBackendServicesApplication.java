package com.server.agentbackendservices;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.server.agentbackendservices.modules.**.mapper")
public class AgentBackendServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentBackendServicesApplication.class, args);
    }
}