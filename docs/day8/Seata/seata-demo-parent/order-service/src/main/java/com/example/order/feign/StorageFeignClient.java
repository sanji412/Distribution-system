package com.example.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务远程调用客户端
 * name: 服务名称
 * url: 服务地址（无注册中心时直接指定）
 */
@FeignClient(name = "storage-service", url = "http://localhost:8082")
public interface StorageFeignClient {

    /**
     * 调用库存服务扣减库存
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 操作结果
     */
    @PostMapping("/storage/decrease")
    String decrease(@RequestParam("productId") Long productId,
                    @RequestParam("count") Integer count);
}