package com.buu.order.client;

import com.buu.order.common.R;
import com.buu.order.dto.RemoteProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品中心 Feign 客户端
 * 通过 Nacos 服务名调用 product-center 的商品查询接口。
 */
@FeignClient(name = "product-center")
public interface ProductFeignClient {

    /**
     * 查询商品详情
     *
     * @param productId 商品 ID
     * @return 商品中心返回的商品信息
     */
    @GetMapping("/api/product/{productId}")
    R<RemoteProductDTO> detail(@PathVariable("productId") Long productId);
}
