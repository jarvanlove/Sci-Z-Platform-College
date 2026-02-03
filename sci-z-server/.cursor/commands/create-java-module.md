# 创建 Java 模块（DDD）

## 概述

按 DDD 架构创建完整 Java 模块。

## 实现顺序

1. Entity → 继承 BaseEntity，与表字段一一对应
2. Mapper → 继承 BaseMapper<Xxx>
3. Repository → 接口 + 实现（组合 Mapper）
4. DTO → Request(CreateReq/UpdateReq/QueryReq) + Response(Resp/DetailResp/PageResp)
5. Converter → MapStruct(componentModel="spring")
6. Service → 接口 + 实现（事务、编排、事件）
7. Controller → 入参校验、鉴权、调 Service、返回 Result

## 检查清单

- [ ] 文件命名符合 ddd-architecture.mdc
- [ ] 注释符合 notes-rules.mdc
- [ ] 日志用 String.format；异常用 BusinessException(ResultCode.X)
- [ ] Service 写操作加 @Transactional(rollbackFor = Exception.class)
- [ ] Controller 返回 Result<T>；禁止全限定名，必须 import
