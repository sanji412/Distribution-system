package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 意图识别结果
 * 保存意图编码、槽位参数和缺参追问话术。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiIntentResult {

    private String intent;
    private Map<String, String> slots = new LinkedHashMap<>();
    private boolean complete;
    private String replyTip;
}
