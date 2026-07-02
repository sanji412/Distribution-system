package com.buu.order.service;

import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.AiChatRequest;
import com.buu.order.dto.AiChatResponse;
import com.buu.order.dto.RemoteBrowseHistoryDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.AiChatRecord;
import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.mapper.AiChatRecordMapper;
import com.buu.order.service.impl.AiCustomerServiceImpl;
import com.buu.order.service.impl.AiPromptTemplateServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCustomerServiceImplTest {

    @Test
    void chatQueriesOrderLogisticsWithExtractedOrderNoAndRecordsConversation() {
        OrderMain order = new OrderMain();
        order.setOrderId(1L);
        order.setOrderNo("DD202507010001");
        order.setUserId(1L);
        order.setProductName("机械键盘-Keychron K3");
        order.setProductNum(2);
        order.setTotalAmount(new BigDecimal("736.00"));
        order.setOrderStatus("已发货");
        order.setLogisticsCompany("顺丰速运");
        order.setLogisticsNo("SF202507010001");
        order.setLogisticsStatus("运输中");
        order.setCurrentLocation("北京市顺义区集散中心");
        order.setExpectArriveTime(LocalDateTime.of(2026, 7, 1, 18, 30));

        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        item.setProductId(1L);

        FakeAiModelClient modelClient = new FakeAiModelClient(
                """
                {"intent":"order_logistics","slots":{"orderNo":"DD202507010001"},"is_complete":true,"reply_tip":null}
                """,
                "亲亲，订单 DD202507010001 已发货，当前在北京市顺义区集散中心运输中，预计 2026-07-01 18:30 前送达哦~"
        );
        AiChatRecordMapper recordMapper = mock(AiChatRecordMapper.class);
        when(recordMapper.insert(any(AiChatRecord.class))).thenReturn(1);

        AiCustomerService service = new AiCustomerServiceImpl(
                new AiPromptTemplateServiceImpl(),
                modelClient,
                new FakeOrderRemoteLocalQuery(order, item),
                new FakeProductFeignClient(),
                new FakeStockFeignClient(),
                recordMapper
        );

        AiChatResponse response = service.chat(new AiChatRequest(1L, "我的订单DD202507010001到哪了？"));

        assertThat(response.getIntent()).isEqualTo("order_logistics");
        assertThat(response.isComplete()).isTrue();
        assertThat(response.getSlots()).containsEntry("orderNo", "DD202507010001");
        assertThat(response.getReply()).contains("DD202507010001").contains("顺义区");
        assertThat(response.getDataSource()).isEqualTo("order_db");
        assertThat(response.getModelUsed()).isEqualTo("deepseek-chat");
        verify(recordMapper).insert(any(AiChatRecord.class));
    }

    private record FakeOrderRemoteLocalQuery(OrderMain order, OrderItem item) implements OrderRemoteLocalQuery {

        @Override
        public OrderMain getOrderByOrderNo(String orderNo) {
            return orderNo.equals(order.getOrderNo()) ? order : null;
        }

        @Override
        public OrderItem getFirstItemByOrderId(Long orderId) {
            return orderId.equals(order.getOrderId()) ? item : null;
        }
    }

    private static class FakeAiModelClient implements AiModelClient {

        private final Queue<String> replies = new ArrayDeque<>();

        private FakeAiModelClient(String... replies) {
            this.replies.addAll(List.of(replies));
        }

        @Override
        public String complete(String prompt) {
            return replies.remove();
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public String modelName() {
            return "deepseek-chat";
        }
    }

    private static class FakeProductFeignClient implements ProductFeignClient {

        @Override
        public R<RemoteProductDTO> detail(Long productId) {
            RemoteProductDTO product = new RemoteProductDTO();
            product.setProductId(productId);
            product.setProductName("机械键盘-Keychron K3");
            product.setPrice(new BigDecimal("368.00"));
            return R.success(product);
        }

        @Override
        public R<List<RemoteBrowseHistoryDTO>> listBrowseHistory(Long userId) {
            return R.success(List.of());
        }
    }

    private static class FakeStockFeignClient implements StockFeignClient {

        @Override
        public R<List<RemoteStockDTO>> listByProductId(Long productId) {
            RemoteStockDTO stock = new RemoteStockDTO();
            stock.setProductId(productId);
            stock.setStockNum(50);
            return R.success(List.of(stock));
        }

        @Override
        public R<Map<String, Object>> deductStock(Long productId, Integer quantity) {
            return R.success(Map.of("deducted", true));
        }

        @Override
        public R<Long> countUndoLog() {
            return R.success(0L);
        }
    }
}
