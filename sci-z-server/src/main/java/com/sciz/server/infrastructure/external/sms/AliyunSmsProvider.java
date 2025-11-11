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
        DefaultProfile profile = DefaultProfile.getProfile(config.regionId(), config.accessKeyId(),
                config.accessKeySecret());
        this.smsClient = new DefaultAcsClient(profile);
    }

    @Override
    public void send(String phone, String code) {
        try {
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", config.regionId());
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", config.signName());
            request.putQueryParameter("TemplateCode", config.templateCode());
            request.putQueryParameter("TemplateParam", buildTemplateParam(code));
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
            log.error(String.format("阿里云短信发送异常: phone=%s, err=%s", phone, exception.getMessage()), exception);
            throw new BusinessException(ResultCode.SMS_CODE_SEND_FAILED, exception.getMessage());
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
