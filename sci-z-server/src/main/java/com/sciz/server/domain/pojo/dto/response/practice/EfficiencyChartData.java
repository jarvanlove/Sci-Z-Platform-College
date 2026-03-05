package com.sciz.server.domain.pojo.dto.response.practice;

import java.util.List;

/**
 * 产教研智能体 - 效率图表数据（柱状图 x 轴标签与 y 轴数值）
 *
 * @param x 横轴标签，如 ["完成率", "里程碑", "周期(天)", "产出"]
 * @param y 纵轴数值，与 x 一一对应
 */
public record EfficiencyChartData(
        List<String> x,
        List<Number> y) {
}
