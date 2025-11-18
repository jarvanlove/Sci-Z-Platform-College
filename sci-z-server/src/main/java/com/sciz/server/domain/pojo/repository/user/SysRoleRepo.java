package com.sciz.server.domain.pojo.repository.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import java.util.List;

/**
 * 角色仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysRoleRepo
 * @date 2025-10-30 11:00
 */
public interface SysRoleRepo {

    /**
     * 保存角色
     *
     * @param entity SysRole 实体
     * @return 生成的主键ID
     */
    Long save(SysRole entity);

    /**
     * 批量按ID查询角色
     *
     * @param ids List<Long> 角色ID集合
     * @return List<SysRole> 角色集合
     */
    List<SysRole> findByIds(List<Long> ids);

    /**
     * 根据角色编码和行业类型查询角色
     *
     * @param roleCode     String 角色编码
     * @param industryType String 行业类型
     * @return SysRole 角色实体（可能为空）
     */
    SysRole findByCode(String roleCode, String industryType);

    /**
     * 查询指定行业下的角色列表
     *
     * @param industryType String 行业类型
     * @return List<SysRole> 角色列表
     */
    List<SysRole> listByIndustryType(String industryType);

    /**
     * 根据ID查询角色
     *
     * @param id Long 角色ID
     * @return SysRole 角色实体
     */
    SysRole findById(Long id);

    /**
     * 根据ID更新角色
     *
     * @param entity SysRole 角色实体
     * @return boolean 是否更新成功
     */
    boolean updateById(SysRole entity);

    /**
     * 根据ID软删除角色
     *
     * @param id Long 角色ID
     * @return boolean 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 分页查询角色列表
     *
     * @param page         IPage<SysRole> 分页对象
     * @param industryType String 行业类型
     * @param keyword      String 搜索关键字（角色名称/角色编码）
     * @param status       Integer 角色状态（null表示全部）
     * @param sortBy       String 排序字段
     * @param asc          boolean 是否升序
     * @return IPage<SysRole> 分页结果
     */
    IPage<SysRole> page(IPage<SysRole> page, String industryType, String keyword, Integer status, String sortBy,
            boolean asc);
}
