package com.sciz.server.infrastructure.config.database;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 序列自动修复配置
 * 
 * 功能：应用启动时自动检查并修复所有表的自增序列
 * 问题：数据迁移时手动指定了 id 值，导致序列的当前值小于表中最大的 id
 * 解决：自动将序列的当前值设置为表中最大的 id 值
 * 
 * @author JiaWen.Wu
 * @className SequenceAutoFixConfig
 * @date 2025-12-25 14:00
 */
@Slf4j
@Component
@Order(1) // 确保在其他组件初始化之前执行
@ConditionalOnProperty(name = "app.sequence.auto-fix.enabled", havingValue = "true", matchIfMissing = true)
public class SequenceAutoFixConfig {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SequenceAutoFixConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 应用启动时自动修复序列
     */
    @PostConstruct
    public void autoFixSequences() {
        log.info("开始检查并修复数据库序列...");

        try {
            // 获取所有以 _id_seq 结尾的序列
            String sql = """
                    SELECT sequencename
                    FROM pg_sequences
                    WHERE schemaname = 'public'
                      AND sequencename LIKE '%_id_seq'
                    ORDER BY sequencename
                    """;

            List<String> sequences = jdbcTemplate.queryForList(sql, String.class);

            if (sequences.isEmpty()) {
                log.info("未找到需要修复的序列");
                return;
            }

            int fixedCount = 0;
            int skippedCount = 0;

            for (String sequenceName : sequences) {
                try {
                    // 从序列名提取表名（去掉 _id_seq 后缀）
                    String tableName = sequenceName.replace("_id_seq", "");

                    // 检查表是否存在
                    String checkTableSql = """
                            SELECT COUNT(*)
                            FROM information_schema.tables
                            WHERE table_schema = 'public'
                              AND table_name = ?
                            """;
                    Integer tableExists = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);

                    if (tableExists == null || tableExists == 0) {
                        log.debug("跳过序列 {}：表 {} 不存在", sequenceName, tableName);
                        skippedCount++;
                        continue;
                    }

                    // 检查表是否有 id 字段
                    String checkIdColumnSql = """
                            SELECT COUNT(*)
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = ?
                              AND column_name = 'id'
                            """;
                    Integer hasIdColumn = jdbcTemplate.queryForObject(checkIdColumnSql, Integer.class, tableName);

                    if (hasIdColumn == null || hasIdColumn == 0) {
                        log.debug("跳过序列 {}：表 {} 没有 id 字段", sequenceName, tableName);
                        skippedCount++;
                        continue;
                    }

                    // 获取表中最大的 id 值
                    String maxIdSql = "SELECT COALESCE(MAX(id), 0) FROM " + tableName;
                    Long maxId = jdbcTemplate.queryForObject(maxIdSql, Long.class);

                    if (maxId == null || maxId == 0) {
                        log.debug("跳过序列 {}：表 {} 为空或最大 id 为 0", sequenceName, tableName);
                        skippedCount++;
                        continue;
                    }

                    // 获取序列的当前值
                    String currentValueSql = "SELECT last_value FROM " + sequenceName;
                    Long currentValue = jdbcTemplate.queryForObject(currentValueSql, Long.class);

                    // 如果序列值小于最大 id，则修复
                    if (currentValue != null && currentValue < maxId) {
                        String fixSql = "SELECT setval(?, ?, false)";
                        jdbcTemplate.queryForObject(fixSql, Long.class, sequenceName, maxId);
                        log.info("已修复序列: {} (表: {}, 原值: {}, 新值: {})",
                                sequenceName, tableName, currentValue, maxId);
                        fixedCount++;
                    } else {
                        log.debug("序列正常: {} (表: {}, 当前值: {}, 最大 id: {})",
                                sequenceName, tableName, currentValue, maxId);
                        skippedCount++;
                    }

                } catch (Exception e) {
                    log.warn("修复序列失败: {}, 错误: {}", sequenceName, e.getMessage());
                    skippedCount++;
                }
            }

            log.info("序列检查完成：修复 {} 个序列，跳过 {} 个序列", fixedCount, skippedCount);

        } catch (Exception e) {
            log.error("自动修复序列时发生异常: {}", e.getMessage(), e);
        }
    }
}
