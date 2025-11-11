package com.server.agentbackendservices.modules.config;//package com.server.agentbackendservices.config;
//
//import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.reflection.MetaObject;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
///**
// * MyBatis Plus 自动填充处理器
// * 自动填充创建时间、更新时间、创建人、更新人等字段
// *
// * @author AgentBackendServices
// * @since 2024-01-01
// */
//@Slf4j
//@Component
//public class MyMetaObjectHandler implements MetaObjectHandler {
//
//    /**
//     * 插入时的填充策略
//     */
//    @Override
//    public void insertFill(MetaObject metaObject) {
//        log.info("开始插入填充...");
//
//        // 填充创建时间
//        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
//
//        // 填充更新时间
//        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
//
//        // 填充创建人（这里可以从SecurityContext或其他地方获取当前用户）
//        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
//
//        // 填充更新人
//        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
//
//        // 填充版本号
//        this.strictInsertFill(metaObject, "version", Integer.class, 1);
//    }
//
//    /**
//     * 更新时的填充策略
//     */
//    @Override
//    public void updateFill(MetaObject metaObject) {
//        log.info("开始更新填充...");
//
//        // 填充更新时间
//        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
//
//        // 填充更新人
//        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
//    }
//
//    /**
//     * 获取当前用户
//     * 这里可以根据实际需求从SecurityContext、JWT Token等地方获取
//     *
//     * @return 当前用户名
//     */
//    private String getCurrentUser() {
//        // TODO: 从SecurityContext或其他地方获取当前用户
//        // 这里暂时返回系统用户
//        return "system";
//    }
//}
