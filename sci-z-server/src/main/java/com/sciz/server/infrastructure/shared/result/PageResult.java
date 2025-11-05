package com.sciz.server.infrastructure.shared.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装类，实现MyBatis-Plus的IPage接口
 *
 * @author JiaWen.Wu
 * @className PageResult
 * @date 2025-10-29 10:00
 */
@Data
public class PageResult<T> implements IPage<T> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 总记录数
     */
    private long total = 0;

    /**
     * 当前页码
     */
    private long current = 1;

    /**
     * 每页大小
     */
    private long size = 10;

    /**
     * 总页数
     */
    private long pages = 0;

    /**
     * 默认构造函数
     */
    public PageResult() {
    }

    /**
     * 构造函数
     *
     * @param current 当前页码
     * @param size    每页大小
     */
    public PageResult(long current, long size) {
        this.current = current;
        this.size = size;
    }

    /**
     * 构造函数
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     */
    public PageResult(List<T> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 从IPage创建PageResult
     *
     * @param page IPage对象
     * @return PageResult
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建空的分页结果
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return PageResult
     */
    public static <T> PageResult<T> empty(long current, long size) {
        return new PageResult<>(Collections.emptyList(), 0, current, size);
    }

    /**
     * 创建空的分页结果
     *
     * @return PageResult
     */
    public static <T> PageResult<T> empty() {
        return empty(1, 10);
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public IPage<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public IPage<T> setTotal(long total) {
        this.total = total;
        this.pages = (total + this.size - 1) / this.size;
        return this;
    }

    @Override
    public long getCurrent() {
        return this.current;
    }

    @Override
    public IPage<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public IPage<T> setSize(long size) {
        this.size = size;
        this.pages = (this.total + size - 1) / size;
        return this;
    }

    @Override
    public long getPages() {
        return this.pages;
    }

    @Override
    public IPage<T> setPages(long pages) {
        this.pages = pages;
        return this;
    }

    /**
     * 是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * 是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return this.current < this.pages;
    }

    @Override
    public List<com.baomidou.mybatisplus.core.metadata.OrderItem> orders() {
        return Collections.emptyList();
    }
}
