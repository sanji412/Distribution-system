package com.buu.order.client;

import com.buu.order.common.R;
import com.buu.order.dto.RemoteStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 库存中心 Feign 客户端
 * 通过 Nacos 服务名调用 stock-center 的库存查询接口。
 */
@FeignClient(name = "stock-center")
public interface StockFeignClient {

    /**
     * 查询指定商品库存
     *
     * @param productId 商品 ID
     * @return 库存中心返回的库存列表
     */
    @GetMapping("/api/stock/product/{productId}")
    R<List<RemoteStockDTO>> listByProductId(@PathVariable("productId") Long productId);
}
