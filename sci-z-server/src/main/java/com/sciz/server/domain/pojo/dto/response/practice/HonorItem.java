package com.sciz.server.domain.pojo.dto.response.practice;

/**
 * 产教研智能体 - 荣誉项（占位，后续可接业务表）
 *
 * @param title 荣誉名称
 * @param level 级别
 * @param date  日期
 */
public record HonorItem(
        String title,
        String level,
        String date) {
}
