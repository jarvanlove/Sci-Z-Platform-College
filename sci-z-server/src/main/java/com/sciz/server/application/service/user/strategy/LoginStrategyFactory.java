package com.sciz.server.application.service.user.strategy;

import com.sciz.server.infrastructure.shared.enums.LoginTypeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录策略工厂
 * 使用 Spring 自动注入所有 LoginStrategy 实现，并通过 Map 管理
 *
 * @author JiaWen.Wu
 * @className LoginStrategyFactory
 * @date 2025-01-15 14:30
 */
@Slf4j
@Component
public class LoginStrategyFactory {

    private final Map<String, LoginStrategy> strategyMap;

    /**
     * 构造函数：通过 Spring 自动注入所有 LoginStrategy 实现
     *
     * @param strategies List<LoginStrategy> 所有登录策略实现
     */
    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        LoginStrategy::getLoginType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn(String.format("发现重复的登录策略类型: %s，使用第一个实现", existing.getLoginType()));
                            return existing;
                        }
                ));
        log.info(String.format("登录策略工厂初始化完成，已注册 %d 种登录方式: %s",
                strategyMap.size(), strategyMap.keySet()));
    }

    /**
     * 根据登录类型获取对应的登录策略
     *
     * @param loginType 登录类型代码（如 "password"、"sms"）
     * @return LoginStrategy 登录策略
     */
    public LoginStrategy getStrategy(String loginType) {
        var type = LoginTypeStatus.fromCode(loginType);
        var strategy = strategyMap.get(type.getCode());
        if (strategy == null) {
            log.error(String.format("未找到登录策略: loginType=%s", loginType));
            throw new IllegalArgumentException(String.format("不支持的登录类型: %s", loginType));
        }
        return strategy;
    }
}

