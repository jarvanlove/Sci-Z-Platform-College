package com.sciz.server.infrastructure.external.dify.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowResponse;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Dify 工作流响应构建器
 * 用于解析工作流响应，封装通用逻辑
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowRespBuilder
 * @date 2025-01-24 19:00
 */
@Slf4j
public final class DifyWorkflowRespBuilder {

    private DifyWorkflowRespBuilder() {
    }

    /**
     * 从 JSON 字符串解析工作流响应
     *
     * @param jsonString   String JSON 字符串
     * @param objectMapper ObjectMapper Jackson 对象映射器
     * @return DifyWorkflowResponse 工作流响应对象
     */
    public static DifyWorkflowResponse parseFromJson(String jsonString, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(jsonString, DifyWorkflowResponse.class);
        } catch (JsonProcessingException e) {
            log.error(String.format("解析工作流响应失败: body=%s, err=%s", jsonString, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "解析工作流响应失败: " + e.getMessage());
        }
    }

    /**
     * 从工作流响应中提取 outputs 作为 Map
     * 用于获取动态字段（如 researchField、researchDirecti、researchTopic）
     * outputs 可能包含工作流自定义的输出字段，这些字段不在 DifyWorkflowResponse.WorkflowOutputs 的固定定义中
     *
     * @param objectMapper ObjectMapper Jackson 对象映射器
     * @param originalJson String 原始 JSON 字符串
     * @return Map<String, Object> outputs 映射
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractOutputsAsMap(ObjectMapper objectMapper, String originalJson) {
        try {
            // 从原始 JSON 中解析 outputs
            JsonNode rootNode = objectMapper.readTree(originalJson);
            JsonNode dataNode = rootNode.get("data");
            if (dataNode == null) {
                log.error(String.format("工作流响应中缺少 data 节点: %s", originalJson));
                throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 data 节点");
            }

            if (!dataNode.has("outputs")) {
                log.error(String.format("工作流响应中缺少 outputs 节点: %s", originalJson));
                throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 outputs 节点");
            }

            JsonNode outputsNode = dataNode.get("outputs");
            return objectMapper.convertValue(outputsNode, Map.class);
        } catch (JsonProcessingException e) {
            log.error(String.format("提取 outputs 映射失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "提取 outputs 映射失败: " + e.getMessage());
        }
    }

    /**
     * 从 outputs Map 中提取指定字段的值
     *
     * @param outputs   Map<String, Object> outputs 映射
     * @param fieldName String 字段名称
     * @return String 字段值，如果不存在则返回空字符串
     */
    public static String extractFieldFromOutputs(Map<String, Object> outputs, String fieldName) {
        if (outputs == null || !outputs.containsKey(fieldName)) {
            return "";
        }
        Object value = outputs.get(fieldName);
        return value != null ? value.toString() : "";
    }

    /**
     * 验证 HTTP 响应状态
     * <p>
     * 检查 HTTP 状态码是否为 2xx，以及响应体是否不为空
     *
     * @param responseEntity ResponseEntity<String> HTTP 响应实体
     * @param operationName  String 操作名称（用于错误日志，如："申报工作流执行"、"红头文件解析工作流执行"）
     * @return String 响应体内容
     * @throws BusinessException 如果 HTTP 状态码不是 2xx 或响应体为空
     */
    public static String validateHttpResponse(ResponseEntity<String> responseEntity, String operationName) {
        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            log.error(String.format("%s失败: statusCode=%s, body=%s",
                    operationName, responseEntity.getStatusCode(), responseEntity.getBody()));
            throw BusinessException.of(ResultCode.SERVER_ERROR,
                    "%s失败: %s", operationName, responseEntity.getBody());
        }
        return responseEntity.getBody();
    }

    /**
     * 检查工作流响应状态
     * <p>
     * 检查 data 节点是否存在，以及工作流执行状态是否为 "succeeded"
     *
     * @param response       DifyWorkflowResponse 工作流响应
     * @param workflowResult String 工作流执行结果（JSON字符串，用于错误日志）
     * @throws BusinessException 如果 data 节点不存在或工作流执行失败
     */
    public static void validateWorkflowStatus(DifyWorkflowResponse response, String workflowResult) {
        // 检查 data 节点
        if (response.getData() == null) {
            log.error(String.format("工作流响应中缺少 data 节点: %s", workflowResult));
            throw new BusinessException(ResultCode.SERVER_ERROR, "工作流响应格式错误：缺少 data 节点");
        }

        // 检查工作流执行状态
        if (!"succeeded".equals(response.getData().getStatus())) {
            String errorMsg = response.getData().getError();
            log.error(String.format("工作流执行失败: status=%s, error=%s",
                    response.getData().getStatus(), errorMsg));
            throw BusinessException.of(ResultCode.SERVER_ERROR,
                    "工作流执行失败: %s", errorMsg != null ? errorMsg : response.getData().getStatus());
        }
    }
}
