package com.example.nacosorderconsumer.config;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类：远程调用工具
 */
@Configuration
public class RestTemplateConfig {

    /**
     * @LoadBalanced：开启客户端负载均衡（通过服务名调用）
     * Sentinel自动适配该RestTemplate，实现远程调用熔断
     */
    @Bean
    @LoadBalanced
    @SentinelRestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}