package com.example.nacosorderconsumer.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务控制器：调用商品服务实现下单
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    // 商品服务地址（通过服务名调用）
    private static final String GOODS_SERVICE = "http://nacos-goods-provider";

    /**
     * 创建订单接口（添加Sentinel熔断降级）
     * @SentinelResource：
     * - value：资源名
     * - blockHandler：限流处理方法
     * - fallback：远程调用失败（异常）的降级处理方法
     */
    @GetMapping("/create/{goodsId}")
    @SentinelResource(
            value = "orderCreate",
            blockHandler = "orderCreateBlockHandler",
            fallback = "orderCreateFallback"
    )
    public String createOrder(@PathVariable Long goodsId) {
        // 远程调用商品服务
        String url = GOODS_SERVICE + "/goods/" + goodsId;
        Map result = restTemplate.getForObject(url, Map.class);
        int i = 1/0;
        return "下单成功，商品信息：" + result;
    }

    /**
     * 订单创建接口限流处理方法
     */
    public String orderCreateBlockHandler(Long goodsId, BlockException e) {
        return "下单失败！请求太频繁，请稍后再试（限流保护），商品ID：" + goodsId;
    }

    /**
     * 订单创建接口降级处理方法（远程调用失败时触发）
     * 要求：参数和原方法一致，额外可加Throwable参数
     */
    public String orderCreateFallback(Long goodsId, Throwable e) {
        // 降级逻辑：返回默认提示，不抛异常
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("code", 200);
        fallbackResult.put("msg", "商品服务暂时不可用，已触发降级保护");
        fallbackResult.put("goodsId", goodsId);
        fallbackResult.put("error", e.getClass().getSimpleName());
        return "下单降级处理：" + fallbackResult;
    }
}