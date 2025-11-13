package com.sciz.server.domain.pojo.dto.request;

import jakarta.validation.constraints.Min;

/**
 * 通用分页查询请求
 *
 * <p>
 * 提供统一的分页与排序入参，供各业务查询请求继承或组合使用。
 * 通过紧凑构造器补齐默认值，避免调用方重复校验初始参数。
 * </p>
 *
 * @param pageNo    Integer 页码，从 1 开始，默认 1
 * @param pageSize  Integer 每页数量，默认 10
 * @param sortBy    String 排序字段
 * @param sortOrder String 排序方式，支持 ASC 或 DESC，默认 DESC
 *
 * @author JiaWen.Wu
 * @className BaseQueryReq
 * @date 2025-11-11 15:00
 */
public record BaseQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder) {

    public BaseQueryReq {
        pageNo = pageNo == null ? 1 : pageNo;
        pageSize = pageSize == null ? 10 : pageSize;
        sortOrder = normalizeSortOrder(sortOrder);
    }

    private static String normalizeSortOrder(String sortOrder) {
        if (sortOrder == null || sortOrder.isBlank()) {
            return "DESC";
        }
        String upper = sortOrder.toUpperCase();
        return switch (upper) {
            case "ASC", "DESC" -> upper;
            default -> "DESC";
        };
    }

    /**
     * 构建默认分页请求
     *
     * @return BaseQueryReq 默认分页参数
     */
    public static BaseQueryReq defaultPage() {
        return new BaseQueryReq(1, 10, null, "DESC");
    }

    /**
     * 快速创建分页请求
     *
     * @param pageNo    Integer 页码
     * @param pageSize  Integer 每页数量
     * @param sortBy    String 排序字段
     * @param sortOrder String 排序方式
     * @return BaseQueryReq 分页请求
     */
    public static BaseQueryReq of(Integer pageNo, Integer pageSize, String sortBy, String sortOrder) {
        return new BaseQueryReq(pageNo, pageSize, sortBy, sortOrder);
    }
}
