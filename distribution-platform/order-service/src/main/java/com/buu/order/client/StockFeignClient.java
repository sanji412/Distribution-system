package com.buu.order.client;

import com.buu.order.client.fallback.StockFeignClientFallback;
import com.buu.order.common.R;
import com.buu.order.dto.RemoteStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 库存中心 Feign 客户端
 * 通过 Nacos 服务名调用 stock-center 的库存查询接口。
 */
@FeignClient(name = "stock-center", fallback = StockFeignClientFallback.class)
public interface StockFeignClient {

    /**
     * 查询指定商品库存
     *
     * @param productId 商品 ID
     * @return 库存中心返回的库存列表
     */
    @GetMapping("/api/stock/product/{productId}")
    R<List<RemoteStockDTO>> listByProductId(@PathVariable("productId") Long productId);

    /**
     * 扣减商品库存
     *
     * @param productId 商品 ID
     * @param quantity 扣减数量
     * @return 库存中心返回的扣减结果
     */
    @PostMapping("/api/stock/deduct")
    R<Map<String, Object>> deductStock(@RequestParam("productId") Long productId,
                                       @RequestParam("quantity") Integer quantity);

    /**
     * 查询库存库 Seata undo_log 记录数
     *
     * @return 库存中心返回的 undo_log 数量
     */
    @GetMapping("/api/stock/seata/undo-log/count")
    R<Long> countUndoLog();
}
