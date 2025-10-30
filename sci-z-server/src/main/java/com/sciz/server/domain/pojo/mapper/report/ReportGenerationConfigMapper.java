package com.sciz.server.domain.pojo.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.report.ReportGenerationConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报告生成配置 Mapper
 *
 * @author JiaWen.Wu
 * @className ReportGenerationConfigMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface ReportGenerationConfigMapper extends BaseMapper<ReportGenerationConfig> {
}
