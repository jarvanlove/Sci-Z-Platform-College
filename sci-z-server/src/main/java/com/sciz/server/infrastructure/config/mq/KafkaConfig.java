package com.sciz.server.infrastructure.config.mq;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 生产者配置
 * 
 * @author JiaWen.Wu
 * @className KafkaConfig
 * @date 2025-10-29 17:10
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * 创建 Kafka 生产者工厂
     * 使用懒加载，避免启动时立即连接 Kafka
     *
     * @return ProducerFactory
     */
    @Bean
    @Lazy
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        // 以下几个属性建议在 application.yml 中覆盖：bootstrap-servers、acks、retries、linger.ms
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        
        // 连接超时配置（缩短超时时间，避免启动时长时间等待）
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000); // 请求超时 3 秒
        props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000); // 连接最大空闲时间 9 分钟
        props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 300000); // 元数据最大年龄 5 分钟
        
        // 连接重试配置（减少重试次数和间隔）
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 50); // 重连退避时间 50ms
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // 重试退避时间 100ms
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * 创建 Kafka 模板
     * 使用懒加载，避免启动时立即初始化
     *
     * @param pf ProducerFactory
     * @return KafkaTemplate
     */
    @Bean
    @Lazy
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }
}
