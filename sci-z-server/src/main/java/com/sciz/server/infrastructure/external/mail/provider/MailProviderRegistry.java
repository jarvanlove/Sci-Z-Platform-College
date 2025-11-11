package com.sciz.server.infrastructure.external.mail.provider;

import com.sciz.server.infrastructure.shared.enums.MailProviderType;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 邮件服务商注册表
 *
 * @author JiaWen.Wu
 * @className MailProviderRegistry
 * @date 2025-11-11 21:30
 */
public class MailProviderRegistry {

    private final Map<MailProviderType, MailProvider> providerMap = new EnumMap<>(MailProviderType.class);

    public MailProviderRegistry(List<MailProvider> providers) {
        Optional.ofNullable(providers)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .forEach(provider -> providerMap.put(provider.type(), provider));
    }

    /**
     * 根据类型获取邮件服务商
     *
     * @param type MailProviderType 服务商类型
     * @return MailProvider 服务商
     */
    public MailProvider getProvider(MailProviderType type) {
        return Optional.ofNullable(providerMap.get(type))
                .orElseThrow(() -> new BusinessException(ResultCode.EMAIL_PROVIDER_NOT_CONFIGURED,
                        String.format("邮箱服务商[%s]未配置", type)));
    }
}
