package com.sciz.server.domain.pojo.repository.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sciz.server.domain.pojo.entity.user.SysUser;

/**
 * 用户仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysUserRepo
 * @date 2025-10-30 11:00
 */
public interface SysUserRepo {

    /**
     * 保存用户
     *
     * @param entity SysUser 实体
     * @return 生成的主键ID
     */
    Long save(SysUser entity);

    /**
     * 按用户名查询
     *
     * @param username 用户名
     * @return SysUser 或 null
     */
    SysUser findByUsername(String username);

    /**
     * 按邮箱查询
     *
     * @param email 邮箱
     * @return SysUser 或 null
     */
    SysUser findByEmail(String email);

    /**
     * 按手机号查询
     *
     * @param phone 手机号
     * @return SysUser 或 null
     */
    SysUser findByPhone(String phone);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return SysUser 或 null
     */
    SysUser findById(Long id);

    /**
     * 更新用户
     *
     * @param entity SysUser 实体
     * @return 是否更新成功
     */
    boolean updateById(SysUser entity);

    /**
     * 分页查询用户列表
     *
     * @param page    IPage<SysUser> 分页对象
     * @param keyword String 搜索关键字（用户名/邮箱/手机号）
     * @param roleId  Long 角色ID（null表示全部）
     * @param status  Integer 用户状态（null表示全部）
     * @param sortBy  String 排序字段
     * @param asc     boolean 是否升序
     * @return IPage<SysUser> 分页结果
     */
    IPage<SysUser> page(IPage<SysUser> page, String keyword, Long roleId, Integer status, String sortBy, boolean asc);
}
