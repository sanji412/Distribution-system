package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 智能推荐响应
 * 基于用户浏览历史生成可展示的推荐卡片。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponse {

    private Long userId;
    private String dataSource;
    private String modelUsed;
    private LocalDateTime createTime;
    private List<Item> items;

    /**
     * 推荐卡片
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        private String title;
        private String content;
    }
}
