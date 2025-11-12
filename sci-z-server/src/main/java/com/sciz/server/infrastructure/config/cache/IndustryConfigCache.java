package com.sciz.server.infrastructure.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.domain.pojo.entity.user.SysProfileField;
import com.sciz.server.domain.pojo.entity.user.SysProfileFieldOption;
import com.sciz.server.domain.pojo.repository.user.SysProfileFieldRepo;
import com.sciz.server.domain.pojo.repository.user.SysConfigRepo;
import com.sciz.server.domain.pojo.repository.user.SysDepartmentRepo;
import com.sciz.server.infrastructure.shared.constant.CacheConstant;
import com.sciz.server.infrastructure.shared.utils.RedisUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
@Slf4j
@Component
public class IndustryConfigCache {

    private final SysConfigRepo sysConfigRepo;
    private final StringRedisTemplate redis;
    private final SysDepartmentRepo sysDepartmentRepo;
    private final SysProfileFieldRepo sysProfileFieldRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IndustryConfigCache(SysConfigRepo sysConfigRepo, StringRedisTemplate redis,
            SysDepartmentRepo sysDepartmentRepo, SysProfileFieldRepo sysProfileFieldRepo) {
        this.sysConfigRepo = sysConfigRepo;
        this.redis = redis;
        this.sysDepartmentRepo = sysDepartmentRepo;
        this.sysProfileFieldRepo = sysProfileFieldRepo;
    }

    /**
     * 获取行业配置（优先从 Redis；未命中则回源 DB 并回写缓存）
     *
     * @return IndustryView 行业视图
     */
    public IndustryView get() {
        return Optional.ofNullable(RedisUtil.get(redis, CacheConstant.INDUSTRY_CONFIG_CURRENT_KEY))
                .flatMap(this::parseCachedView)
                .orElseGet(() -> {
                    var view = loadFromDb();
                    cache(view);
                    return view;
                });
    }

    /**
     * 手动刷新缓存（如后台保存行业配置后调用）
     */
    public void refresh() {
        cache(loadFromDb());
    }

    /**
     * 将行业视图写入 Redis 缓存
     *
     * @param view IndustryView 行业视图
     */
    private void cache(IndustryView view) {
        try {
            var json = objectMapper.writeValueAsString(view);
            RedisUtil.set(redis, CacheConstant.INDUSTRY_CONFIG_CURRENT_KEY, json,
                    Duration.ofSeconds(CacheConstant.INDUSTRY_CONFIG_CACHE_EXPIRE));
            log.info(String.format("行业配置已缓存: type=%s, name=%s", view.getType(), view.getName()));
        } catch (Exception e) {
            log.error(String.format("缓存行业配置失败: err=%s", e.getMessage()), e);
        }
    }

    /**
     * 从数据库加载当前行业配置键值
     *
     * @return IndustryView 行业视图
     */
    private IndustryView loadFromDb() {
        var configKeys = new String[] {
                CacheConstant.CONFIG_KEY_INDUSTRY_TYPE,
                CacheConstant.CONFIG_KEY_INDUSTRY_NAME,
                CacheConstant.CONFIG_KEY_LABEL_DEPT,
                CacheConstant.CONFIG_KEY_LABEL_ROLE,
                CacheConstant.CONFIG_KEY_LABEL_EMP
        };

        var configMap = new HashMap<String, String>();
        for (var key : configKeys) {
            Optional.ofNullable(sysConfigRepo.findByKey(key))
                    .map(config -> config.getConfigValue())
                    .ifPresent(value -> configMap.put(key, value));
        }

        var view = new IndustryView();
        view.setType(configMap.getOrDefault(CacheConstant.CONFIG_KEY_INDUSTRY_TYPE, "education"));
        view.setName(configMap.getOrDefault(CacheConstant.CONFIG_KEY_INDUSTRY_NAME, "教育行业"));
        view.setDepartmentLabel(configMap.getOrDefault(CacheConstant.CONFIG_KEY_LABEL_DEPT, "院系"));
        view.setRoleLabel(configMap.getOrDefault(CacheConstant.CONFIG_KEY_LABEL_ROLE, "角色"));
        view.setEmployeeIdLabel(configMap.getOrDefault(CacheConstant.CONFIG_KEY_LABEL_EMP, "学工号"));

        var departments = sysDepartmentRepo.listByIndustryType(view.getType());
        var departmentOptions = departments.stream()
                .map(dept -> {
                    var option = new DepartmentOption();
                    option.setDepartmentName(dept.getDepartmentName());
                    option.setDepartmentCode(dept.getDepartmentCode());
                    return option;
                })
                .collect(Collectors.toList());
        view.setDepartments(departmentOptions);

        var profileFields = buildProfileFieldViews(view.getType());
        view.setProfileFields(profileFields);

        log.info(String.format("从数据库加载行业配置成功: type=%s, name=%s, departmentCount=%s, fieldCount=%s",
                view.getType(), view.getName(), departmentOptions.size(), profileFields.size()));
        return view;
    }

    private List<ProfileFieldView> buildProfileFieldViews(String industryType) {
        var fields = sysProfileFieldRepo.listEnabledByIndustry(industryType);
        if (fields.isEmpty()) {
            return List.of();
        }
        var fieldIds = fields.stream()
                .map(SysProfileField::getId)
                .toList();
        var options = sysProfileFieldRepo.listOptionsByFieldIds(fieldIds);
        Map<Long, List<SysProfileFieldOption>> optionMap = options.stream()
                .collect(Collectors.groupingBy(SysProfileFieldOption::getFieldId));
        return fields.stream()
                .map(field -> {
                    var view = new ProfileFieldView();
                    view.setFieldCode(field.getFieldCode());
                    view.setFieldLabel(field.getFieldLabel());
                    view.setFieldType(field.getFieldType());
                    view.setIsRequired(field.getIsRequired());
                    view.setOptions(Optional.ofNullable(optionMap.get(field.getId()))
                            .orElseGet(List::of)
                            .stream()
                            .map(option -> {
                                var optionView = new ProfileFieldOptionView();
                                optionView.setOptionValue(option.getOptionValue());
                                optionView.setOptionLabel(option.getOptionLabel());
                                optionView.setIsDefault(option.getIsDefault());
                                return optionView;
                            })
                            .toList());
                    return view;
                })
                .toList();
    }

    /**
     * 解析缓存的行业视图
     *
     * @param json String 缓存的 JSON 字符串
     * @return Optional<IndustryView> 行业视图
     */
    private Optional<IndustryView> parseCachedView(String json) {
        try {
            var view = objectMapper.readValue(json, IndustryView.class);
            if (view == null) {
                return Optional.empty();
            }
            return Optional.of(view);
        } catch (Exception e) {
            log.warn(String.format("解析行业配置缓存失败，将从数据库重新加载: err=%s", e.getMessage()));
            return Optional.empty();
        }
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

        /**
         * 行业下的部门列表
         */
        private List<DepartmentOption> departments;

        /**
         * 行业下的扩展字段配置
         */
        private List<ProfileFieldView> profileFields;
    }

    /**
     * 部门选项视图
     */
    @Data
    public static class DepartmentOption {
        /**
         * 部门名称
         */
        private String departmentName;
        /**
         * 部门编码
         */
        private String departmentCode;
    }

    /**
     * 扩展字段视图
     */
    @Data
    public static class ProfileFieldView {
        /**
         * 字段编码
         */
        private String fieldCode;
        /**
         * 字段展示名称
         */
        private String fieldLabel;
        /**
         * 字段类型
         */
        private String fieldType;
        /**
         * 是否必填
         */
        private Integer isRequired;
        /**
         * 选项列表
         */
        private List<ProfileFieldOptionView> options;
    }

    /**
     * 扩展字段选项视图
     */
    @Data
    public static class ProfileFieldOptionView {
        /**
         * 选项值
         */
        private String optionValue;
        /**
         * 选项名称
         */
        private String optionLabel;
        /**
         * 是否默认
         */
        private Integer isDefault;
    }
}
