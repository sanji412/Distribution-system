package com.buu.order.service;

import com.buu.order.client.PayFeignClient;
import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.OrderRemoteDetailDTO;
import com.buu.order.dto.RemotePaymentDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.service.impl.OrderRemoteQueryServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class OrderRemoteQueryServiceImplTest {

    @Test
    void getRemoteDetailCallsProductStockAndPayServicesByFeign() {
        OrderMain order = new OrderMain();
        order.setOrderId(1L);
        order.setOrderNo("DD202505190001");
        order.setProductName("机械键盘-Keychron K3");
        order.setProductNum(2);
        order.setTotalAmount(new BigDecimal("736.00"));
        order.setOrderStatus("已支付");

        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        item.setProductId(1L);

        RemoteProductDTO product = new RemoteProductDTO();
        product.setProductId(1L);
        product.setProductName("机械键盘-Keychron K3");
        product.setPrice(new BigDecimal("368.00"));

        RemoteStockDTO stock = new RemoteStockDTO();
        stock.setProductId(1L);
        stock.setWarehouseId(1L);
        stock.setStockNum(50);

        RemotePaymentDTO payment = new RemotePaymentDTO();
        payment.setOrderNo("DD202505190001");
        payment.setPayStatus("SUCCESS");

        FakeOrderRemoteLocalQuery localQuery = new FakeOrderRemoteLocalQuery(order, item);
        FakeProductFeignClient productFeignClient = new FakeProductFeignClient(R.success(product));
        FakeStockFeignClient stockFeignClient = new FakeStockFeignClient(R.success(List.of(stock)));
        FakePayFeignClient payFeignClient = new FakePayFeignClient(R.success(payment));

        OrderRemoteQueryServiceImpl service = new OrderRemoteQueryServiceImpl(
                localQuery,
                productFeignClient,
                stockFeignClient,
                payFeignClient
        );

        OrderRemoteDetailDTO detail = service.getRemoteDetail("DD202505190001");

        assertThat(detail.getOrderNo()).isEqualTo("DD202505190001");
        assertThat(detail.getProduct().getProductName()).isEqualTo("机械键盘-Keychron K3");
        assertThat(detail.getStocks()).hasSize(1);
        assertThat(detail.getPayment().getPayStatus()).isEqualTo("SUCCESS");
        assertThat(detail.getProductService()).isEqualTo("product-center");
        assertThat(detail.getStockService()).isEqualTo("stock-center");
        assertThat(detail.getPayService()).isEqualTo("pay-center");

        assertThat(productFeignClient.calledProductId).isEqualTo(1L);
        assertThat(stockFeignClient.calledProductId).isEqualTo(1L);
        assertThat(payFeignClient.calledOrderNo).isEqualTo("DD202505190001");
    }

    private record FakeOrderRemoteLocalQuery(OrderMain order, OrderItem item) implements OrderRemoteLocalQuery {

        @Override
        public OrderMain getOrderByOrderNo(String orderNo) {
            return order;
        }

        @Override
        public OrderItem getFirstItemByOrderId(Long orderId) {
            return item;
        }
    }

    private static class FakeProductFeignClient implements ProductFeignClient {

        private final R<RemoteProductDTO> response;
        private Long calledProductId;

        private FakeProductFeignClient(R<RemoteProductDTO> response) {
            this.response = response;
        }

        @Override
        public R<RemoteProductDTO> detail(Long productId) {
            this.calledProductId = productId;
            return response;
        }
    }

    private static class FakeStockFeignClient implements StockFeignClient {

        private final R<List<RemoteStockDTO>> response;
        private Long calledProductId;

        private FakeStockFeignClient(R<List<RemoteStockDTO>> response) {
            this.response = response;
        }

        @Override
        public R<List<RemoteStockDTO>> listByProductId(Long productId) {
            this.calledProductId = productId;
            return response;
        }
    }

    private static class FakePayFeignClient implements PayFeignClient {

        private final R<RemotePaymentDTO> response;
        private String calledOrderNo;

        private FakePayFeignClient(R<RemotePaymentDTO> response) {
            this.response = response;
        }

        @Override
        public R<RemotePaymentDTO> detailByOrderNo(String orderNo) {
            this.calledOrderNo = orderNo;
            return response;
        }
    }
}
