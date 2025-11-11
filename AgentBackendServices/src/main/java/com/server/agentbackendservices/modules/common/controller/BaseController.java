package com.server.agentbackendservices.modules.common.controller;

import com.server.agentbackendservices.modules.common.entity.BaseEntity;
import com.server.agentbackendservices.modules.common.mapper.MyBaseMapper;
import com.server.agentbackendservices.modules.common.service.BaseServiceImpl;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@SuppressWarnings("all")
@Tag(name = "base", description = "base相关的API")
public abstract class BaseController<M extends MyBaseMapper<T>,T extends BaseEntity> {

    @Autowired
    BaseServiceImpl<M,T> service;

    @PostMapping("/list")
    @Operation(summary = "获取列表", description = "获取列表")
    public ResultVO list(@RequestBody T entity) {
        if (entity == null) {
            //  throw new HttpMessageNotReadableException("请求体不能为空", null);
        }
        return service.getWhereList(entity);
    }

//    @Autowired
//    public BaseController(@Qualifier("UserServiceImpl") S service) {
//        this.service = service;
//    }
    @PostMapping("/save")
    @Operation(summary = "新增数据", description = "新增数据")
    public ResultVO save(@RequestBody T entity) {
        if (entity == null) {
            //  throw new HttpMessageNotReadableException("请求体不能为空", null);
        }
        return ResultVO.ok( service.save(entity)) ;
    }

    @PutMapping("/update")
    @Operation(summary = "根据id修改数据", description = "根据id修改数据")
    public ResultVO update(@RequestBody T entity) {
        return ResultVO.ok( service.updateById(entity))  ;
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "根据id删除数据", description = "根据id删除数据")
    public ResultVO delete(@PathVariable Long id) {
        return  ResultVO.ok( service.removeById(id)) ;
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "根据id获取数据", description = "根据id获取数据")
    public ResultVO getById(@PathVariable Long id) {
        return  ResultVO.ok( service.getById(id)) ;
    }


}
