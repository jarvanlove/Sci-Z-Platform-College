package com.sciz.server.infrastructure.external.dify.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申报工作流输入参数配置
 * 类型安全的工作流输入参数定义
 *
 * @param researchFields    DifyStringArrayInput 研究领域列表
 * @param researchDirection DifyStringInput 研究方向
 * @param researchTopic     DifyStringInput 研究课题
 * @author JiaWen.Wu
 * @className DeclarationWorkflowReq
 * @date 2025-01-26 15:00
 */
public record DeclarationWorkflowReq(
        DifyStringArrayInput researchFields,
        DifyStringInput researchDirection,
        DifyStringInput researchTopic) {

    /**
     * 创建申报工作流输入参数
     *
     * @param researchFields    List<String> 研究领域列表
     * @param researchDirection String 研究方向
     * @param researchTopic     String 研究课题
     * @return DeclarationWorkflowReq 工作流输入参数
     */
    public static DeclarationWorkflowReq of(List<String> researchFields, String researchDirection,
            String researchTopic) {
        return new DeclarationWorkflowReq(
                new DifyStringArrayInput(researchFields),
                new DifyStringInput(researchDirection),
                new DifyStringInput(researchTopic));
    }

    /**
     * 转换为 Map（用于 DifyWorkflowRequest）
     * <p>
     * 注意：researchField 在工作流中需要是字符串类型（逗号分隔），
     * 虽然前端传的是数组，但这里需要转换为逗号分隔的字符串
     *
     * @return Map<String, Object> 工作流输入参数 Map
     */
    public Map<String, Object> toInputsMap() {
        var inputs = new HashMap<String, Object>();

        // 将研究领域数组转换为逗号分隔的字符串（工作流要求）
        var researchFieldList = researchFields.values();
        var researchFieldString = researchFieldList != null && !researchFieldList.isEmpty()
                ? String.join(",", researchFieldList) // 使用英文逗号分隔
                : "";
        inputs.put("researchField", researchFieldString);

        inputs.put("researchDirection", researchDirection.value());
        inputs.put("researchTopic", researchTopic.value());
        return inputs;
    }
}
