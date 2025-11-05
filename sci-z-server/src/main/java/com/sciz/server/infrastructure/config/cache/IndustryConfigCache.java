package com.sciz.server.infrastructure.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.domain.pojo.entity.user.SysConfig;
import com.sciz.server.domain.pojo.repository.user.SysConfigRepo;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 行业配置提供器
 *
 * 统一从数据库 sys_config 读取一次后写入 Redis，后续业务从缓存读取。
 * 数据基本稳定，设置较长 TTL，人工变更后可手动清理 key 触发重载。
 *
 * @author JiaWen.Wu
 * @className IndustryConfigCache
 * @date 2025-10-31 10:30
 */
@Component
public class IndustryConfigCache {

    // 命名空间
    public static final String KEY_NAMESPACE = "sciz:cfg:industry";
    // 当前行业整体配置
    public static final String KEY_CURRENT = KEY_NAMESPACE + ":current";

    // 这些是我们关心的配置键
    private static final String K_TYPE = "current_industry_type";
    private static final String K_NAME = "current_industry_name";
    private static final String K_LABEL_DEPT = "label.department";
    private static final String K_LABEL_ROLE = "label.role";
    private static final String K_LABEL_EMP = "label.employee_id";

    private final SysConfigRepo sysConfigRepo;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IndustryConfigCache(SysConfigRepo sysConfigRepo, StringRedisTemplate redis) {
        this.sysConfigRepo = sysConfigRepo;
        this.redis = redis;
    }

    /**
     * 获取行业配置（优先从 Redis；未命中则回源 DB 并回写缓存）
     *
     * @return IndustryView 行业视图
     */
    public IndustryView get() {
        String json = RedisUtil.get(redis, KEY_CURRENT);
        if (json != null) {
            try {
                return objectMapper.readValue(json, IndustryView.class);
            } catch (Exception ignore) {
                // 解析异常则回源
            }
        }
        IndustryView view = loadFromDb();
        cache(view);
        return view;
    }

    /**
     * 手动刷新缓存（如后台保存行业配置后调用）
     *
     * @return void
     */
    public void refresh() {
        cache(loadFromDb());
    }

    /**
     * 将行业视图写入 Redis 缓存
     *
     * @param view IndustryView 行业视图
     * @return void
     */
    private void cache(IndustryView view) {
        try {
            String json = objectMapper.writeValueAsString(view);
            // TTL 一天；若需要常驻可将 ttl 置为 null
            RedisUtil.set(redis, KEY_CURRENT, json, Duration.ofDays(1));
        } catch (Exception ignore) {
        }
    }

    /**
     * 从数据库加载当前行业配置键值
     *
     * @return IndustryView 行业视图
     */
    private IndustryView loadFromDb() {
        Map<String, String> kv = new HashMap<>();
        for (String k : new String[] { K_TYPE, K_NAME, K_LABEL_DEPT, K_LABEL_ROLE, K_LABEL_EMP }) {
            SysConfig cfg = sysConfigRepo.findByKey(k);
            if (cfg != null && cfg.getConfigValue() != null) {
                kv.put(k, cfg.getConfigValue());
            }
        }
        IndustryView view = new IndustryView();
        view.setType(kv.getOrDefault(K_TYPE, "education"));
        view.setName(kv.getOrDefault(K_NAME, "教育行业"));
        view.setDepartmentLabel(kv.getOrDefault(K_LABEL_DEPT, "院系"));
        view.setRoleLabel(kv.getOrDefault(K_LABEL_ROLE, "角色"));
        view.setEmployeeIdLabel(kv.getOrDefault(K_LABEL_EMP, "学工号"));
        return view;
    }

    /**
     * 简化版视图，供前端/业务使用
     */
    @Data
    public static class IndustryView {
        /**
         * 行业类型
         */
        private String type;
        /**
         * 行业名称
         */
        private String name;
        /**
         * 部门标签
         */
        private String departmentLabel;
        /**
         * 角色标签
         */
        private String roleLabel;
        /**
         * 员工ID标签
         */
        private String employeeIdLabel;
    }
}
