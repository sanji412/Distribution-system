package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 客服 Prompt 模板响应
 * 用于展示意图识别规则和业务动态 Prompt。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptTemplateResponse {

    private List<IntentDefinition> intents;
    private String intentRecognitionPrompt;
    private String customerServicePrompt;
    private String orderLogisticsPrompt;
    private String stockPrompt;

    /**
     * C 端客服可识别意图定义
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntentDefinition {

        private String intent;
        private String name;
        private String api;
        private String requiredSlots;
        private String description;
    }
}
