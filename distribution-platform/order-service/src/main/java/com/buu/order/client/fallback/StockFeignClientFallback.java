package com.buu.order.client.fallback;

import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.RemoteStockDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存中心 Feign 降级处理
 * 当 stock-center 不可用或被 Sentinel 熔断时，返回安全库存快照。
 */
@Component
public class StockFeignClientFallback implements StockFeignClient {

    /**
     * 查询指定商品库存的降级结果
     *
     * @param productId 商品 ID
     * @return 降级库存数据
     */
    @Override
    public R<List<RemoteStockDTO>> listByProductId(Long productId) {
        RemoteStockDTO fallbackStock = new RemoteStockDTO();
        fallbackStock.setProductId(productId);
        fallbackStock.setWarehouseId(-1L);
        fallbackStock.setStockNum(0);

        R<List<RemoteStockDTO>> result = R.success(List.of(fallbackStock));
        result.setMsg("库存服务暂不可用，已触发 Sentinel Feign 降级回调");
        return result;
    }
}
