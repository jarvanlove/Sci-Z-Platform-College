package com.sciz.server.domain.pojo.repository.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.report.ReportManagement;

/**
 * 报告管理仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ReportManagementRepo
 * @date 2025-10-30 11:00
 */
public interface ReportManagementRepo {

    /**
     * 保存报告管理
     *
     * @param entity ReportManagement 实体
     * @return 生成的主键ID
     */
    Long save(ReportManagement entity);

    /**
     * 根据ID查询报告管理
     *
     * @param id Long 报告ID
     * @return ReportManagement 报告实体
     */
    ReportManagement findById(Long id);

    /**
     * 分页查询报告列表
     *
     * @param page    Page<ReportManagement> 分页对象
     * @param keyword String 搜索关键字（报告编号/项目名称/创建人）
     * @param status  String 报告状态（null表示全部）
     * @param reportType String 报告类型（null表示全部）
     * @param sortBy  String 排序字段
     * @param asc     boolean 是否升序
     * @return IPage<ReportManagement> 分页结果
     */
    IPage<ReportManagement> page(Page<ReportManagement> page, String keyword, String status, String reportType, String sortBy, boolean asc);

    /**
     * 更新报告管理
     *
     * @param entity ReportManagement 实体
     * @return boolean 是否更新成功
     */
    boolean updateById(ReportManagement entity);

    /**
     * 根据ID删除报告（软删除）
     *
     * @param id Long 报告ID
     * @return boolean 是否删除成功
     */
    boolean deleteById(Long id);
}
