package com.buu.order.service.impl;

import com.buu.order.service.AiModelClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Spring AI DeepSeek 调用客户端
 * 通过 OpenAI 兼容协议调用 DeepSeek Chat 模型。
 */
@Service
public class SpringAiModelClient implements AiModelClient {

    private final ChatClient chatClient;
    private final String apiKey;
    private final String modelName;

    public SpringAiModelClient(ChatClient.Builder chatClientBuilder,
                               @Value("${spring.ai.openai.api-key:}") String apiKey,
                               @Value("${spring.ai.openai.chat.options.model:deepseek-chat}") String modelName) {
        this.chatClient = chatClientBuilder.build();
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    @Override
    public String complete(String prompt) {
        if (!available()) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public boolean available() {
        return StringUtils.hasText(apiKey)
                && !apiKey.contains("placeholder")
                && !apiKey.contains("你的API密钥");
    }

    @Override
    public String modelName() {
        return modelName;
    }
}
