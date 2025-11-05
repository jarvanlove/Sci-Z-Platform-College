package com.sciz.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author JiaWen.Wu
 * @className ServerApplication
// * @date 2025-10-28 00:00
 */
@MapperScan({
        "com.sciz.server.infrastructure.external.dify.mapper"
})
@SpringBootApplication
public class ServerApplication {

    /**
     * 应用启动入口
     *
     * @param args 命令行参数
     * @return void
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
