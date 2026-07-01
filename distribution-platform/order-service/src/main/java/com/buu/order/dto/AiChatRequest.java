package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 客服对话请求
 * 承载用户身份和当前输入内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    private Long userId;
    private String message;
}
