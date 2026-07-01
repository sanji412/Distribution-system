package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 客服对话响应
 * 返回意图识别结果、业务数据来源和最终客服回复。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String reply;
    private String intent;
    private Map<String, String> slots;
    private boolean complete;
    private String dataSource;
    private String modelUsed;
    private LocalDateTime createTime;
}
