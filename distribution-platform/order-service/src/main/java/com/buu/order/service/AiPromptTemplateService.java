package com.buu.order.service;

import com.buu.order.dto.AiPromptTemplateResponse;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.OrderMain;

import java.util.List;

/**
 * AI 客服 Prompt 模板服务
 * 基于当前 Controller 接口能力生成可展示、可执行的 Prompt。
 */
public interface AiPromptTemplateService {

    /**
     * 查询 Prompt 模板和意图规则
     *
     * @return 模板响应
     */
    AiPromptTemplateResponse getPromptTemplate();

    /**
     * 构建意图识别 Prompt
     *
     * @param userMessage 用户问题
     * @return 可直接发送给模型的 Prompt
     */
    String buildIntentRecognitionPrompt(String userMessage);

    /**
     * 构建订单物流动态 Prompt
     *
     * @param userMessage 用户问题
     * @param order       订单真实数据
     * @return 动态 Prompt
     */
    String buildOrderLogisticsPrompt(String userMessage, OrderMain order);

    /**
     * 构建商品库存动态 Prompt
     *
     * @param userMessage 用户问题
     * @param product     商品真实数据
     * @param stocks      库存真实数据
     * @return 动态 Prompt
     */
    String buildStockPrompt(String userMessage, RemoteProductDTO product, List<RemoteStockDTO> stocks);

    /**
     * 构建通用客服 Prompt
     *
     * @param userMessage 用户问题
     * @return 通用客服 Prompt
     */
    String buildGeneralCustomerPrompt(String userMessage);
}
