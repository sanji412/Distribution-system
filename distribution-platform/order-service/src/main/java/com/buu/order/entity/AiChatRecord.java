package com.buu.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话记录实体
 * 记录订单客服问答内容，为 AI 调用统计和历史对话查询提供数据。
 */
@Data
@TableName("ai_chat_record")
public class AiChatRecord {

    @TableId(value = "chat_id", type = IdType.AUTO)
    private Long chatId;
    private Long userId;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}
