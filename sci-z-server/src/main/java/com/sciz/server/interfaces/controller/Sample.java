package com.sciz.server.interfaces.controller;


import com.aliyun.tea.*;
import com.aliyun.alimt20181012.*;
import com.aliyun.alimt20181012.models.*;
import com.aliyun.teaopenapi.*;
import com.aliyun.teaopenapi.models.*;


public class Sample {
    public static com.aliyun.alimt20181012.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 将 ALIBABA_CLOUD_ACCESS_KEY_ID 替换为 AccessKey ID。
                .setAccessKeyId ("LTAI5tPHxqGBScDfNswjkbhz")
                // 将 ALIBABA_CLOUD_ACCESS_KEY_SECRET 替换为 AccessKey Secret。
                .setAccessKeySecret ("dtTVmlLfyf7MYpxiHqAU9ItCkg92Cz");
        // Endpoint 请参考 https://api.aliyun.com/product/alimt
        config.endpoint = "mt.cn-hangzhou.aliyuncs.com";
        return new com.aliyun.alimt20181012.Client(config);
    }
    public static void main(String[] args_) throws Exception {
        java.util.List<String> args = java.util.Arrays.asList(args_);
        com.aliyun.alimt20181012.Client client = Sample.createClient();
        TranslateGeneralRequest request = new TranslateGeneralRequest()
                .setFormatType("text")
                .setSourceLanguage("zh")
                .setTargetLanguage("en")
                .setSourceText("你好")
                .setScene("general");
        TranslateGeneralResponse response = client.translateGeneral(request);
        System.out.println(response.body.data.translated);

    }
}