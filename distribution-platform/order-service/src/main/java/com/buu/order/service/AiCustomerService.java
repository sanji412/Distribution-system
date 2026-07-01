package com.buu.order.service;

import com.buu.order.dto.AiChatRequest;
import com.buu.order.dto.AiChatResponse;

/**
 * AI 客服业务接口
 * 负责意图识别、业务查询和客服回复生成。
 */
public interface AiCustomerService {

    /**
     * 处理用户对话
     *
     * @param request 用户问题
     * @return AI 客服回复
     */
    AiChatResponse chat(AiChatRequest request);
}
