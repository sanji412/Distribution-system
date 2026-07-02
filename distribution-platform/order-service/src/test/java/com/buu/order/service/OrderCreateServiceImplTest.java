package com.buu.order.service;

import com.buu.order.client.PayFeignClient;
import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.OrderCreateRequest;
import com.buu.order.dto.OrderCreateResponse;
import com.buu.order.dto.PaymentCreateRequest;
import com.buu.order.dto.RemotePaymentDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.entity.OrderMain;
import com.buu.order.mapper.OrderItemMapper;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.SeataTransactionRecordService;
import com.buu.order.service.impl.OrderCreateServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceImplTest {

    @Mock
    private OrderMainMapper orderMainMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private StockFeignClient stockFeignClient;
    @Mock
    private PayFeignClient payFeignClient;
    @Mock
    private SeataTransactionRecordService seataTransactionRecordService;

    @Test
    void createOrderMethodIsGlobalTransactionalEntry() throws NoSuchMethodException {
        Method method = OrderCreateServiceImpl.class.getDeclaredMethod("createOrder", OrderCreateRequest.class);

        GlobalTransactional annotation = method.getAnnotation(GlobalTransactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.rollbackFor()).contains(Exception.class);
    }

    @Test
    void createOrderPersistsOrderDeductsStockCreatesPaymentAndMarksPaid() {
        RemoteProductDTO product = new RemoteProductDTO();
        product.setProductId(1L);
        product.setProductName("机械键盘-Keychron K3");
        product.setPrice(new BigDecimal("368.00"));
        given(productFeignClient.detail(1L)).willReturn(R.success(product));

        given(orderMainMapper.insert(any(OrderMain.class))).willAnswer(invocation -> {
            OrderMain order = invocation.getArgument(0);
            order.setOrderId(88L);
            return 1;
        });
        given(orderItemMapper.insert(any())).willReturn(1);
        given(stockFeignClient.deductStock(1L, 2)).willReturn(R.success(Map.of("deducted", true)));

        RemotePaymentDTO payment = new RemotePaymentDTO();
        payment.setPayNo("PAY202607020001");
        payment.setPayStatus("支付成功");
        given(payFeignClient.createPayment(any(PaymentCreateRequest.class))).willReturn(R.success(payment));
        given(orderMainMapper.updateById(any(OrderMain.class))).willReturn(1);

        OrderCreateService service = new OrderCreateServiceImpl(
                orderMainMapper,
                orderItemMapper,
                productFeignClient,
                stockFeignClient,
                payFeignClient,
                seataTransactionRecordService
        );
        OrderCreateResponse response = service.createOrder(buildRequest(false));

        assertThat(response.getOrderNo()).startsWith("DD");
        assertThat(response.getOrderStatus()).isEqualTo("已支付");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("736.00");
        assertThat(response.getPayment().getPayNo()).isEqualTo("PAY202607020001");

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderMainMapper).insert(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo("待支付");
        assertThat(orderCaptor.getValue().getProductName()).isEqualTo("机械键盘-Keychron K3");

        ArgumentCaptor<OrderMain> paidOrderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderMainMapper).updateById(paidOrderCaptor.capture());
        assertThat(paidOrderCaptor.getValue().getOrderStatus()).isEqualTo("已支付");
        assertThat(paidOrderCaptor.getValue().getPayTime()).isNotNull();

        verify(stockFeignClient).deductStock(1L, 2);
        verify(payFeignClient).createPayment(any(PaymentCreateRequest.class));
        verify(seataTransactionRecordService).recordStarted(any(), any());
        verify(seataTransactionRecordService).markStockSucceeded(any(), any());
        verify(seataTransactionRecordService).markCommitted(any(), any());
    }

    @Test
    void createOrderThrowsWhenPayBranchFailsSoSeataCanRollbackPreviousBranches() {
        RemoteProductDTO product = new RemoteProductDTO();
        product.setProductId(1L);
        product.setProductName("机械键盘-Keychron K3");
        product.setPrice(new BigDecimal("368.00"));
        given(productFeignClient.detail(1L)).willReturn(R.success(product));
        given(orderMainMapper.insert(any(OrderMain.class))).willAnswer(invocation -> {
            OrderMain order = invocation.getArgument(0);
            order.setOrderId(88L);
            return 1;
        });
        given(orderItemMapper.insert(any())).willReturn(1);
        given(stockFeignClient.deductStock(1L, 2)).willReturn(R.success(Map.of("deducted", true)));
        given(payFeignClient.createPayment(any(PaymentCreateRequest.class)))
                .willReturn(R.fail("模拟支付失败"));

        OrderCreateService service = new OrderCreateServiceImpl(
                orderMainMapper,
                orderItemMapper,
                productFeignClient,
                stockFeignClient,
                payFeignClient,
                seataTransactionRecordService
        );

        assertThatThrownBy(() -> service.createOrder(buildRequest(true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("支付");

        verify(orderMainMapper, never()).updateById(any(OrderMain.class));
        verify(seataTransactionRecordService).markRolledBack(any(), any(), any(), any(), any());
    }

    private OrderCreateRequest buildRequest(boolean simulatePayFailure) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setQuantity(2);
        request.setReceiverName("李思润");
        request.setReceiverPhone("13800138000");
        request.setReceiverAddress("北京市顺义区集散中心");
        request.setPayMethod("支付宝");
        request.setSimulatePayFailure(simulatePayFailure);
        return request;
    }
}
