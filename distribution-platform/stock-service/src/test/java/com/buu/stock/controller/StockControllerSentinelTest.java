package com.buu.stock.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.buu.stock.common.R;
import com.buu.stock.dto.SentinelRuleDTO;
import com.buu.stock.entity.Stock;
import com.buu.stock.entity.Warehouse;
import com.buu.stock.service.SentinelRuleQueryService;
import com.buu.stock.service.StockQueryService;
import com.buu.stock.service.WarehouseService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StockControllerSentinelTest {

    @Test
    void stockDeductEndpointDeclaresSentinelResourceAndBlockHandler() throws NoSuchMethodException {
        Method deduct = StockController.class.getDeclaredMethod("deductStock", Long.class, Integer.class);

        SentinelResource resource = deduct.getAnnotation(SentinelResource.class);

        assertThat(resource).isNotNull();
        assertThat(resource.value()).isEqualTo("stockDeduct");
        assertThat(resource.blockHandler()).isEqualTo("stockDeductBlockHandler");
    }

    @Test
    void productStockEndpointDeclaresHotParamResource() throws NoSuchMethodException {
        Method listByProductId = StockController.class.getDeclaredMethod("listByProductId", Long.class);

        SentinelResource resource = listByProductId.getAnnotation(SentinelResource.class);

        assertThat(resource).isNotNull();
        assertThat(resource.value()).isEqualTo("stockByProduct");
        assertThat(resource.blockHandler()).isEqualTo("stockByProductBlockHandler");
    }

    @Test
    void deductStockReturnsBusinessResult() {
        StockController controller = new StockController(
                new FakeStockQueryService(true),
                new FakeWarehouseService(),
                new FakeSentinelRuleQueryService()
        );

        R<Map<String, Object>> result = controller.deductStock(1L, 2);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData())
                .containsEntry("productId", 1L)
                .containsEntry("quantity", 2)
                .containsEntry("deducted", true);
    }

    @Test
    void stockDeductBlockHandlerReturnsLimitMessage() {
        StockController controller = new StockController(
                new FakeStockQueryService(true),
                new FakeWarehouseService(),
                new FakeSentinelRuleQueryService()
        );

        R<Map<String, Object>> result = controller.stockDeductBlockHandler(1L, 2, new FlowException("stockDeduct"));

        assertThat(result.getCode()).isEqualTo(429);
        assertThat(result.getMsg()).contains("Sentinel").contains("限流");
        assertThat(result.getData())
                .containsEntry("productId", 1L)
                .containsEntry("quantity", 2)
                .containsEntry("deducted", false);
    }

    private record FakeStockQueryService(boolean deductResult) implements StockQueryService {

        @Override
        public List<Stock> listStocks() {
            return List.of();
        }

        @Override
        public List<Stock> listByProductId(Long productId) {
            return List.of();
        }

        @Override
        public boolean deductStock(Long productId, Integer quantity) {
            return deductResult;
        }

        @Override
        public Long countUndoLog() {
            return 0L;
        }
    }

    private static class FakeWarehouseService implements WarehouseService {

        @Override
        public List<Warehouse> listWarehouses() {
            return List.of();
        }
    }

    private static class FakeSentinelRuleQueryService implements SentinelRuleQueryService {

        @Override
        public List<SentinelRuleDTO> listRules() {
            return List.of();
        }
    }
}
