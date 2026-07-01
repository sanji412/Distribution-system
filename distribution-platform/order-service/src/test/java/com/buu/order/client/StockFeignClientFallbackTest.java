package com.buu.order.client;

import com.buu.order.client.fallback.StockFeignClientFallback;
import com.buu.order.common.R;
import com.buu.order.dto.RemoteStockDTO;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockFeignClientFallbackTest {

    @Test
    void stockFeignClientUsesFallbackForSentinelDegrade() {
        FeignClient feignClient = StockFeignClient.class.getAnnotation(FeignClient.class);

        assertThat(feignClient).isNotNull();
        assertThat(feignClient.fallback()).isEqualTo(StockFeignClientFallback.class);
    }

    @Test
    void fallbackReturnsSafeStockSnapshotWhenStockServiceUnavailable() {
        StockFeignClientFallback fallback = new StockFeignClientFallback();

        R<List<RemoteStockDTO>> response = fallback.listByProductId(1L);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMsg()).contains("降级");
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getProductId()).isEqualTo(1L);
        assertThat(response.getData().get(0).getWarehouseId()).isEqualTo(-1L);
        assertThat(response.getData().get(0).getStockNum()).isZero();
    }
}
