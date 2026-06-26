package com.buu.distribution.service;

import com.buu.distribution.dto.CreateOrderRequest;
import com.buu.distribution.dto.OrderDetailResponse;
import com.buu.distribution.dto.UpdateOrderStatusRequest;
import com.buu.distribution.entity.OrderItem;
import com.buu.distribution.entity.OrderMain;
import com.buu.distribution.entity.Payment;
import com.buu.distribution.entity.Product;
import com.buu.distribution.entity.Stock;
import com.buu.distribution.exception.BusinessException;
import com.buu.distribution.mapper.OrderItemMapper;
import com.buu.distribution.mapper.OrderMainMapper;
import com.buu.distribution.mapper.PaymentMapper;
import com.buu.distribution.mapper.ProductMapper;
import com.buu.distribution.mapper.StockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private OrderMainMapper orderMainMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderDeductsStockAndCreatesPaymentWhenStockIsEnough() {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("机械键盘 - Keychron K3");
        product.setPrice(new BigDecimal("399.00"));
        product.setStatus(1);

        Stock stock = new Stock();
        stock.setStockId(10L);
        stock.setProductId(1L);
        stock.setWarehouseId(1L);
        stock.setStockNum(10);

        when(productMapper.selectById(1L)).thenReturn(product);
        when(stockMapper.selectOne(any())).thenReturn(stock);
        when(stockMapper.updateById(any(Stock.class))).thenReturn(1);
        doAnswer(invocation -> {
            OrderMain order = invocation.getArgument(0);
            order.setOrderId(100L);
            return 1;
        }).when(orderMainMapper).insert(any(OrderMain.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(paymentMapper.insert(any(Payment.class))).thenReturn(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(2);
        request.setReceiverName("张三");
        request.setReceiverPhone("13800138000");
        request.setReceiverAddress("北京市海淀区中关村南大街5号");

        OrderDetailResponse response = orderService.createOrder(request);

        assertThat(response.getOrderNo()).startsWith("DD");
        assertThat(response.getProductName()).isEqualTo("机械键盘 - Keychron K3");
        assertThat(response.getProductNum()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("798.00");
        assertThat(response.getOrderStatus()).isEqualTo("待支付");

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockMapper).updateById(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getStockNum()).isEqualTo(8);

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderMainMapper).insert(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderStatus()).isEqualTo("待支付");
        assertThat(orderCaptor.getValue().getTotalAmount()).isEqualByComparingTo("798.00");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).insert(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getPayStatus()).isEqualTo("待支付");
        assertThat(paymentCaptor.getValue().getPayAmount()).isEqualByComparingTo("798.00");
        assertThat(paymentCaptor.getValue().getOrderNo()).isEqualTo(response.getOrderNo());
    }

    @Test
    void createOrderThrowsAndDoesNotWriteDataWhenStockIsNotEnough() {
        Product product = new Product();
        product.setProductId(2L);
        product.setProductName("无线鼠标 - 罗技 M720");
        product.setPrice(new BigDecimal("199.00"));
        product.setStatus(1);

        Stock stock = new Stock();
        stock.setStockId(11L);
        stock.setProductId(2L);
        stock.setWarehouseId(1L);
        stock.setStockNum(1);

        when(productMapper.selectById(2L)).thenReturn(product);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setProductId(2L);
        request.setWarehouseId(1L);
        request.setQuantity(2);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存不足");

        verify(stockMapper, never()).updateById(any(Stock.class));
        verify(orderMainMapper, never()).insert(any(OrderMain.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(paymentMapper, never()).insert(any(Payment.class));
    }

    @Test
    void updateStatusKeepsExistingLogisticsFieldsWhenRequestDoesNotProvideThem() {
        OrderMain order = new OrderMain();
        order.setOrderId(100L);
        order.setOrderNo("DD202505190003");
        order.setUserId(1L);
        order.setProductName("铝合金笔记本支架");
        order.setProductNum(1);
        order.setTotalAmount(new BigDecimal("89.00"));
        order.setOrderStatus("已发货");
        order.setLogisticsCompany("顺丰速运");
        order.setLogisticsNo("SF1234567890");
        order.setLogisticsStatus("运输中");
        order.setCurrentLocation("北京市通州区中转场");

        when(orderMainMapper.selectOne(any())).thenReturn(order);
        when(orderMainMapper.updateById(any(OrderMain.class))).thenReturn(1);

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setOrderNo("DD202505190003");
        request.setOrderStatus("已完成");

        OrderDetailResponse response = orderService.updateStatus(request);

        assertThat(response.getOrderStatus()).isEqualTo("已完成");
        assertThat(response.getLogisticsCompany()).isEqualTo("顺丰速运");
        assertThat(response.getLogisticsNo()).isEqualTo("SF1234567890");
        assertThat(response.getLogisticsStatus()).isEqualTo("运输中");
        assertThat(response.getCurrentLocation()).isEqualTo("北京市通州区中转场");

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderMainMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getLogisticsCompany()).isEqualTo("顺丰速运");
        assertThat(orderCaptor.getValue().getLogisticsNo()).isEqualTo("SF1234567890");
    }
}
