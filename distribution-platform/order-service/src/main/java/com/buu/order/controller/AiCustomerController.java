package com.buu.order.controller;

import com.buu.order.common.R;
import com.buu.order.dto.AiChatRequest;
import com.buu.order.dto.AiChatResponse;
import com.buu.order.dto.AiPromptTemplateResponse;
import com.buu.order.service.AiCustomerService;
import com.buu.order.service.AiPromptTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DeepSeek AI 客服 Controller
 * 提供客服对话入口和 Prompt 模板展示接口。
 */
@RestController
@RequestMapping("/api/order/ai")
public class AiCustomerController {

    private final AiCustomerService aiCustomerService;
    private final AiPromptTemplateService aiPromptTemplateService;

    public AiCustomerController(AiCustomerService aiCustomerService, AiPromptTemplateService aiPromptTemplateService) {
        this.aiCustomerService = aiCustomerService;
        this.aiPromptTemplateService = aiPromptTemplateService;
    }

    /**
     * AI 客服对话
     *
     * @param request 用户问题
     * @return 客服回复
     */
    @PostMapping("/chat")
    public R<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return R.success(aiCustomerService.chat(request));
    }

    /**
     * 查询 AI 客服 Prompt 模板
     *
     * @return Prompt 模板和意图规则
     */
    @GetMapping("/prompt-template")
    public R<AiPromptTemplateResponse> promptTemplate() {
        return R.success(aiPromptTemplateService.getPromptTemplate());
    }
}
