package com.server.agentbackendservices.modules.common.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.server.agentbackendservices.mapper.MyBaseMapper;
import com.server.agentbackendservices.modules.common.entity.BaseEntity;
import com.server.agentbackendservices.common.annotation.FuzzyQueries;
import com.server.agentbackendservices.modules.common.vo.ResultVO;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseServiceImpl<M extends MyBaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements BaseService<T> {
    public ResultVO getWhereList(T entity){
        Page<T> page = new Page<>(entity.getPageNum(), entity.getPageSize());
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();

        Field[] declaredFields = getEntityType().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            String name = field.getName();
            if(!(null == field.getAnnotation(FuzzyQueries.class))){
                queryWrapper.or().like(name, entity.getKey());
            }
        }
        if (entity.getWhere()!= null && !entity.getWhere().isEmpty()){
            String where = entity.getWhere();
            Map<String, Condition> stringConditionMap = parseQuery(where);
            for (Map.Entry<String, Condition> entry : stringConditionMap.entrySet()) {
                String field = entry.getKey();
                Condition condition = entry.getValue();
                // 根据字段类型进行类型转换
                Object convertedValue = convertValue(field, condition.getValue());
                
                switch (condition.getOperator()) {
                    case "==":
                        queryWrapper.eq(field, convertedValue);
                        break;
                    case "!=":
                        queryWrapper.ne(field, convertedValue);
                        break;
                    case ">":
                        queryWrapper.gt(field, convertedValue);
                        break;
                    case "<":
                        queryWrapper.lt(field, convertedValue);
                        break;
                    case ">=":
                        queryWrapper.ge(field, convertedValue);
                        break;
                    case "<=":
                        queryWrapper.le(field, convertedValue);
                        break;
                    default:
                        break;
                }
            }
        }

        // 添加排序条件
        if (entity.getOrder() != null && !entity.getOrder().isEmpty()) {
            queryWrapper.orderBy(true, entity.getOrder().contains("asc"), entity.getOrder());
        }

        // 执行查询
        Page<T> resultPage = this.page(page, queryWrapper);
        System.out.println(resultPage.getTotal());
        for (T record : resultPage.getRecords()){
            System.out.println(record);
        }
        return ResultVO.ok(resultPage.getRecords());
    }

    public Class<T> getEntityType() {
        Class<?> clazz = getClass();
        ParameterizedType genericSuperclass = (ParameterizedType) clazz.getGenericSuperclass();
        Type actualClassType = genericSuperclass.getActualTypeArguments()[0];
        return (Class<T>) actualClassType;
    }


    /**
     * 根据字段名进行类型转换
     * @param fieldName 字段名
     * @param value 原始值
     * @return 转换后的值
     */
    private Object convertValue(String fieldName, String value) {
        try {
            // 根据字段名判断类型
            if ("id".equals(fieldName)) {
                return Long.parseLong(value);
            } else if (fieldName.contains("Time") || fieldName.contains("Date")) {
                // 时间类型字段
                return value;
            } else if (fieldName.contains("Status") || fieldName.contains("Type") || fieldName.contains("Deleted")) {
                // 整数类型字段
                return Integer.parseInt(value);
            } else {
                // 默认返回字符串
                return value;
            }
        } catch (NumberFormatException e) {
            // 如果转换失败，返回原始字符串
            return value;
        }
    }

    public static Map<String, Condition> parseQuery(String query) {
        Map<String, Condition> conditions = new HashMap<>();
        String[] pairs = query.split("&&");
        for (String pair : pairs) {
            // 支持 ==、!=、>、<、>=、<= 等操作符
            if (pair.contains("==")) {
                String[] keyValue = pair.split("==");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), "=="));
            } else if (pair.contains("!=")) {
                String[] keyValue = pair.split("!=");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), "!="));
            } else if (pair.contains(">=")) {
                String[] keyValue = pair.split(">=");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), ">="));
            } else if (pair.contains("<=")) {
                String[] keyValue = pair.split("<=");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), "<="));
            } else if (pair.contains(">")) {
                String[] keyValue = pair.split(">");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), ">"));
            } else if (pair.contains("<")) {
                String[] keyValue = pair.split("<");
                conditions.put(keyValue[0].trim(), new Condition(keyValue[1].trim(), "<"));
            }
        }
        return conditions;
    }
         static class Condition {
        private String value;
        private String operator;

        public Condition(String value, String operator) {
            this.value = value;
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public String getOperator() {
            return operator;
        }
    }

}