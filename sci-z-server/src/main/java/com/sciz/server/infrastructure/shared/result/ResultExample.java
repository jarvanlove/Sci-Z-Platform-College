package com.sciz.server.infrastructure.shared.result;

import java.util.Arrays;
import java.util.List;

/**
 * Result使用示例
 *
 * @author JiaWen.Wu
 * @className ResultExample
 * @date 2025-10-29 10:30
 */
public class ResultExample {

    /**
     * 演示Result的各种用法
     */
    public void demonstrateResultUsage() {
        // 1. 成功响应（无数据）
        Result<Void> success1 = Result.success();
        System.out.println("成功无数据: " + success1);

        // 2. 成功响应（带消息）
        Result<Void> success2 = Result.success("操作完成");
        System.out.println("成功带消息: " + success2);

        // 3. 成功响应（带数据）
        String data = "Hello World";
        Result<String> success3 = Result.success(data);
        System.out.println("成功带数据: " + success3);

        // 4. 成功响应（带数据和消息）
        Result<String> success4 = Result.success(data, "数据获取成功");
        System.out.println("成功带数据和消息: " + success4);

        // 5. 失败响应（使用错误码枚举）
        Result<Void> fail1 = Result.fail(ResultCode.USER_NOT_FOUND);
        System.out.println("失败使用枚举: " + fail1);

        // 6. 失败响应（使用错误码枚举和自定义消息）
        Result<Void> fail2 = Result.fail(ResultCode.USER_NOT_FOUND, "用户ID为123的用户不存在");
        System.out.println("失败使用枚举和自定义消息: " + fail2);

        // 7. 失败响应（使用默认错误码）
        Result<Void> fail3 = Result.fail("业务处理失败");
        System.out.println("失败使用默认错误码: " + fail3);

        // 8. 失败响应（使用自定义错误码）
        Result<Void> fail4 = Result.fail(4001, "自定义错误");
        System.out.println("失败使用自定义错误码: " + fail4);
    }

    /**
     * 演示PageResult的用法
     */
    public void demonstratePageResultUsage() {
        // 1. 创建分页结果
        List<String> records = Arrays.asList("item1", "item2", "item3");
        PageResult<String> pageResult = new PageResult<>(records, 100, 1, 10);
        System.out.println("分页结果: " + pageResult);

        // 2. 创建空的分页结果
        PageResult<String> emptyPage = PageResult.empty();
        System.out.println("空分页结果: " + emptyPage);

        // 3. 从IPage创建PageResult
        // 这里假设有一个IPage对象
        // PageResult<String> fromIPage = PageResult.of(iPageObject);

        // 4. 检查分页信息
        System.out.println("是否有上一页: " + pageResult.hasPrevious());
        System.out.println("是否有下一页: " + pageResult.hasNext());
        System.out.println("总页数: " + pageResult.getPages());
    }

    /**
     * 演示ResultCode的用法
     */
    public void demonstrateResultCodeUsage() {
        // 1. 根据错误码获取枚举
        ResultCode resultCode = ResultCode.fromCode(2001);
        System.out.println("错误码2001对应: " + resultCode);

        // 2. 判断错误类型
        System.out.println("是否为成功: " + ResultCode.SUCCESS.isSuccess());
        System.out.println("是否为客户端错误: " + ResultCode.BAD_REQUEST.isClientError());
        System.out.println("是否为服务器错误: " + ResultCode.SERVER_ERROR.isServerError());
        System.out.println("是否为业务错误: " + ResultCode.USER_NOT_FOUND.isBusinessError());

        // 3. 获取错误信息
        System.out.println("错误码: " + ResultCode.USER_NOT_FOUND.getCode());
        System.out.println("错误消息: " + ResultCode.USER_NOT_FOUND.getMessage());
    }
}
