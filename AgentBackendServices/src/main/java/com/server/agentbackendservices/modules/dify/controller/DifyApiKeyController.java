package com.server.agentbackendservices.modules.dify.controller;

import com.server.agentbackendservices.modules.common.controller.BaseController;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
import com.server.agentbackendservices.modules.dify.entity.DifyApiKey;
import com.server.agentbackendservices.modules.dify.mapper.DifyApiKeyMapper;
import com.server.agentbackendservices.modules.dify.service.DifyApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
