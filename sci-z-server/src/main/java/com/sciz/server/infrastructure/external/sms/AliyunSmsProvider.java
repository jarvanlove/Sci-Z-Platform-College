package com.sciz.server.infrastructure.external.sms;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.sciz.server.infrastructure.config.sms.SmsProviderProperties.AliyunConfig;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云短信服务商实现
 *
 * @author JiaWen.Wu
 * @className AliyunSmsProvider
 * @date 2025-11-11 22:10
 */
@Slf4j
public class AliyunSmsProvider implements SmsProvider {

    private final AliyunConfig config;
    private final IAcsClient smsClient;

    public AliyunSmsProvider(AliyunConfig config) {
        this.config = config;
        // 🔥 修复：确保 Secret、签名名称和模板代码没有前后空格（防止配置中的隐藏字符）
        String accessKeySecret = config.accessKeySecretTrimmed();
        String signName = config.signNameTrimmed();
        String templateCode = config.templateCodeTrimmed();
        
        // 验证 Secret 长度（阿里云 AccessKey Secret 标准长度是 30）
        if (accessKeySecret.length() != 30) {
            log.warn(String.format("警告: AccessKey Secret 长度异常: length=%d (期望: 30), 可能导致签名计算失败", accessKeySecret.length()));
        }
        
        // 🔥 修复：检查签名名称是否为空
        if (signName.isEmpty()) {
            log.error("错误: 签名名称为空，请检查配置文件中的 SMS_ALIYUN_SIGN_NAME 配置");
        }
        
        // 🔥 修复：检查模板代码是否为空
        if (templateCode.isEmpty()) {
            log.error("错误: 模板代码为空，请检查配置文件中的 SMS_ALIYUN_TEMPLATE_CODE 配置");
        }
        
        // 🔥 修复：检查签名名称是否有异常字符（帮助发现编码问题）
        String originalSignName = config.signName();
        if (originalSignName != null && !originalSignName.equals(signName)) {
            log.warn(String.format("签名名称包含前后空格，已自动去除: 原始值=[%s] (length=%d), 处理后=[%s] (length=%d)", 
                    originalSignName, originalSignName.length(), signName, signName.length()));
        }
        
        // 🔥 修复：打印签名名称的字节数组（帮助发现编码问题）
        byte[] signNameBytes = signName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String signNameHex = java.util.HexFormat.of().formatHex(signNameBytes);
        log.info(String.format("签名名称字节数组 (UTF-8): %s", signNameHex));
        
        // 打印配置信息用于调试（不打印完整 Secret，只打印前后几位）
        String maskedSecret = accessKeySecret.length() > 8
                ? accessKeySecret.substring(0, 4) + "****" + accessKeySecret.substring(accessKeySecret.length() - 4)
                : "****";
        log.info(String.format("初始化阿里云短信服务: regionId=%s, accessKeyId=%s, accessKeySecret=%s (length=%d), signName=[%s] (length=%d, bytes=%d), templateCode=[%s]",
                config.regionId(), config.accessKeyId(), maskedSecret, accessKeySecret.length(), 
                signName, signName.length(), signNameBytes.length, templateCode));

        // 使用 trim 后的 Secret 创建客户端
        DefaultProfile profile = DefaultProfile.getProfile(config.regionId(), config.accessKeyId(), accessKeySecret);
        this.smsClient = new DefaultAcsClient(profile);
    }

    @Override
    public void send(String phone, String code) {
        try {
            // 🔥 修复：使用 trim 后的签名名称和模板代码
            String signName = config.signNameTrimmed();
            String templateCode = config.templateCodeTrimmed();
            
            log.debug(String.format("准备发送短信: phone=%s, signName=[%s], templateCode=[%s]", phone, signName, templateCode));
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", config.regionId());
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            String templateParam = buildTemplateParam(code);
            request.putQueryParameter("TemplateParam", templateParam);
            log.debug(String.format("短信请求参数: RegionId=%s, PhoneNumbers=%s, SignName=[%s], TemplateCode=[%s], TemplateParam=%s",
                    config.regionId(), phone, signName, templateCode, templateParam));
            CommonResponse response = smsClient.getCommonResponse(request);
            if (response.getHttpStatus() != 200) {
                throw new BusinessException(ResultCode.SMS_CODE_SEND_FAILED, response.getData());
            }
            var payload = JsonUtil.fromJsonToMap(response.getData());
            var codeValue = payload != null ? String.valueOf(payload.get("Code")) : "";
            if (!"OK".equalsIgnoreCase(codeValue)) {
                var message = payload != null ? String.valueOf(payload.get("Message")) : "未知错误";
                log.error(String.format("阿里云短信发送失败: phone=%s, code=%s, err=%s", phone, codeValue, message));
                throw new BusinessException(ResultCode.SMS_CODE_SEND_FAILED, message);
            }
            log.info(String.format("阿里云短信发送成功: phone=%s, requestId=%s", phone,
                    payload != null ? payload.get("RequestId") : ""));
        } catch (BusinessException exception) {
            throw exception;
        } catch (ClientException exception) {
            String errorMsg = exception.getMessage();
            log.error(String.format("阿里云短信发送异常: phone=%s, err=%s", phone, errorMsg), exception);
            
            // 如果是签名不匹配错误，提供更详细的提示
            if (errorMsg != null && errorMsg.contains("SignatureDoesNotMatch")) {
                String maskedSecret = config.accessKeySecret() != null && config.accessKeySecret().length() > 8
                        ? config.accessKeySecret().substring(0, 4) + "****" + config.accessKeySecret().substring(config.accessKeySecret().length() - 4)
                        : "****";
                log.error(String.format("签名不匹配错误 - 请检查 AccessKey Secret 是否正确: accessKeyId=%s, accessKeySecret=%s (length=%d)",
                        config.accessKeyId(), maskedSecret, config.accessKeySecret() != null ? config.accessKeySecret().length() : 0));
            }
            
            throw new BusinessException(ResultCode.SMS_CODE_SEND_FAILED, errorMsg);
        }
    }

    /**
     * 构建模板参数
     *
     * @param code String 验证码
     * @return String 模板参数
     */
    private String buildTemplateParam(String code) {
        String paramName = config.templateParamNameOrDefault();
        return String.format("{\"%s\":\"%s\"}", paramName, code);
    }
}
