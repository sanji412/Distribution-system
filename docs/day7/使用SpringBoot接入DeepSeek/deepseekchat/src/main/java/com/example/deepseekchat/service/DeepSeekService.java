package com.example.deepseekchat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DeepSeekService {

    private final ChatClient chatClient;

    // 构造器注入 ChatClient（Spring AI 自动配置了 Builder）
    public DeepSeekService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 发送用户消息，获取 AI 回复
     */
    public String chat(String userMessage) {
        // 一行代码完成调用，Spring AI 自动处理历史上下文、序列化等
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    public String customerServiceChat(String userMessage) {
        // 客服模板，{userMessage} 是占位符
        String promptTemplate = """
            【角色人设】
            你是优选电商的官方在线客服，称呼用户为「亲亲」，语气亲切、耐心、专业。
            
            【业务范围】
            仅可解答商品咨询、物流查询、售后申请、活动规则四类问题。
            超出范围统一回复：抱歉亲亲，我只能解答购物相关的问题哦~
            
            【回复要求】
            1. 回复简洁，150字以内
            2. 禁止编造物流、优惠等不存在的信息
            
            【用户当前问题】
            {userMessage}
            """;

        // 替换占位符
        String finalPrompt = promptTemplate.replace("{userMessage}", userMessage);

        return chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
    }
}