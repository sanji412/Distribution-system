package com.buu.order.service;

import com.buu.order.dto.AiRecommendationResponse;

/**
 * AI 智能推荐服务
 * 根据商品浏览历史生成推荐话术。
 */
public interface AiRecommendationService {

    /**
     * 查询用户智能推荐
     *
     * @param userId 用户 ID
     * @return 推荐卡片列表
     */
    AiRecommendationResponse recommendForUser(Long userId);
}
