package com.sciz.server.infrastructure.shared.utils;

import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Kafka 工具
 * 
 * @author JiaWen.Wu
 * @className KafkaUtil
 * @date 2025-10-29 17:00
 */
@Slf4j
public final class KafkaUtil {

    private KafkaUtil() {
    }

    /**
     * 发送字符串消息（异步）
     *
     * @param template KafkaTemplate 实例
     * @param topic    String 主题
     * @param message  String 消息内容
     * @return void
     */
    public static void send(KafkaTemplate<String, String> template, String topic, String message) {
        log.info(String.format("Kafka async send topic=%s message=%s", topic, message));
        template.send(topic, message);
    }

    /**
     * 发送带 key 的字符串消息（异步）
     *
     * @param template KafkaTemplate 实例
     * @param topic    String 主题
     * @param key      String 分区键
     * @param message  String 消息内容
     * @return void
     */
    public static void sendWithKey(KafkaTemplate<String, String> template, String topic, String key, String message) {
        log.info(String.format("Kafka async send topic=%s key=%s message=%s", topic, key, message));
        template.send(topic, key, message);
    }

    /**
     * 同步发送（等待服务端响应）
     *
     * @param template KafkaTemplate 实例
     * @param record   ProducerRecord 消息记录
     * @return void
     */
    public static void sendSync(KafkaTemplate<String, String> template, ProducerRecord<String, String> record) {
        log.info(String.format("Kafka sync send topic=%s key=%s message=%s", record.topic(), record.key(),
                record.value()));
        try {
            template.send(record).get();
        } catch (Exception e) {
            log.error(String.format("Kafka sync send failed topic=%s key=%s err=%s", record.topic(), record.key(),
                    e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建 ProducerRecord
     *
     * @param topic String 主题
     * @param key   String key
     * @param value String 消息
     * @return ProducerRecord<String,String> 记录
     */
    public static ProducerRecord<String, String> record(String topic, String key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }
}
