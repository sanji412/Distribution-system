package com.example.nacosgoodsprovider.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @RefreshScope：Nacos配置动态刷新
 * @RestController：标识为Web接口控制器
 */
@RestController
@RequestMapping("/goods")
@RefreshScope
public class GoodsController {

    @Value("${server.port}")
    private String serverPort;

    // 读取远程Nacos中的自定义配置
    @Value("${goods.stock:0}")
    private Integer goodsStock;

    @Value("${goods.desc:默认描述}")
    private String goodsDesc;

    /**
     * 根据ID查询商品接口（添加Sentinel限流）
     * @SentinelResource：标记为Sentinel受保护资源
     * - value：资源名称（自定义，建议和接口名一致）
     * - blockHandler：限流/熔断时的自定义处理方法
     */
    @GetMapping("/{goodsId}")
    @SentinelResource(value = "goodsDetail", blockHandler = "goodsDetailBlockHandler")
    public Map<String, Object> getGoodsById(@PathVariable Long goodsId) {
        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goodsId);
        result.put("goodsName", "华为Mate手机");
        result.put("price", 4999);
        result.put("stock", goodsStock);
        result.put("desc", goodsDesc);
        result.put("serverPort", serverPort);
        return result;
    }

    /**
     * 商品详情接口限流后的处理方法（必须和上面方法注解上的blockHandler名称一致）
     * 要求：
     * 1. 方法参数和原方法一致，额外加BlockException参数
     * 2. 返回值和原方法一致
     */
    public Map<String, Object> goodsDetailBlockHandler(Long goodsId, BlockException e) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", 500);
        errorResult.put("msg", "请求太火爆啦！请稍后再试（限流保护）");
        errorResult.put("goodsId", goodsId);
        errorResult.put("exception", e.getClass().getSimpleName());
        return errorResult;
    }

    /**
     * 测试配置读取接口
     */
    @GetMapping("/config")
    public String testConfig() {
        return "库存：" + goodsStock + "，描述：" + goodsDesc;
    }
}