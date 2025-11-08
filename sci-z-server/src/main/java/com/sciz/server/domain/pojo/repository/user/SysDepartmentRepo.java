package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import java.util.List;

/**
 * 部门仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysDepartmentRepo
 * @date 2025-10-30 11:00
 */
public interface SysDepartmentRepo {

    /**
     * 保存部门
     *
     * @param entity SysDepartment 实体
     * @return 生成的主键ID
     */
    Long save(SysDepartment entity);

    /**
     * 根据ID查询部门
     *
     * @param id Long 部门ID
     * @return SysDepartment 部门实体（可能为 null）
     */
    SysDepartment findById(Long id);

    /**
     * 根据行业类型查询部门列表
     *
     * @param industryType String 行业类型
     * @return java.util.List<SysDepartment> 部门列表
     */
    List<SysDepartment> listByIndustryType(String industryType);

    /**
     * 根据行业类型和部门编码查询
     *
     * @param industryType   String 行业类型
     * @param departmentCode String 部门编码
     * @return SysDepartment 部门实体
     */
    SysDepartment findByCode(String industryType, String departmentCode);
}
