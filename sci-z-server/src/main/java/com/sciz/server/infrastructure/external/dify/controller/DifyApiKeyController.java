package com.sciz.server.infrastructure.external.dify.controller;

import com.sciz.server.infrastructure.external.dify.common.controller.BaseController;
import com.sciz.server.infrastructure.external.dify.entity.DifyApiKey;
import com.sciz.server.infrastructure.external.dify.mapper.DifyApiKeyMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dify API 密钥管理控制器
 *
 * @author shihang.shang
 * @className DifyApiKeyController
 * @date 2025-01-28 12:30
 */
@Slf4j
@RestController
@RequestMapping("/api/dify/keys")
@RequiredArgsConstructor
@Tag(name = "Dify API 密钥管理", description = "Dify API 密钥相关接口")
public class DifyApiKeyController extends BaseController<DifyApiKeyMapper, DifyApiKey> {

}