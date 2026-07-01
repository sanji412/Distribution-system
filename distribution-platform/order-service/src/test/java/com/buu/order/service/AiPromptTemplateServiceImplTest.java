package com.buu.order.service;

import com.buu.order.dto.AiPromptTemplateResponse;
import com.buu.order.service.impl.AiPromptTemplateServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptTemplateServiceImplTest {

    @Test
    void promptTemplateIsGeneratedFromCurrentControllerInterfaces() {
        AiPromptTemplateService service = new AiPromptTemplateServiceImpl();

        AiPromptTemplateResponse response = service.getPromptTemplate();

        assertThat(response.getIntentRecognitionPrompt())
                .contains("/api/order/remote-detail/{orderNo}")
                .contains("/api/stock/product/{productId}")
                .contains("order_logistics")
                .contains("goods_stock")
                .contains("orderNo")
                .contains("productId")
                .contains("用户提问：{{用户输入内容}}");
        assertThat(response.getCustomerServicePrompt())
                .contains("优选电商")
                .contains("禁止编造");
        assertThat(response.getIntents())
                .extracting(AiPromptTemplateResponse.IntentDefinition::getIntent)
                .containsExactly("order_logistics", "goods_stock", "product_consult", "other");
    }
}
