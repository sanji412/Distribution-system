package com.buu.order.service;

/**
 * 大模型调用抽象
 * 将 Spring AI 依赖隔离在实现层，方便测试和降级。
 */
public interface AiModelClient {

    /**
     * 调用对话模型生成文本
     *
     * @param prompt 完整提示词
     * @return 模型返回文本
     */
    String complete(String prompt);

    /**
     * 判断模型配置是否可用
     *
     * @return true 表示可尝试调用模型
     */
    boolean available();

    /**
     * 当前模型名称
     *
     * @return 模型名称
     */
    String modelName();
}
