package com.buu.order.service.impl;

import com.buu.order.client.PayFeignClient;
import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.OrderCreateRequest;
import com.buu.order.dto.OrderCreateResponse;
import com.buu.order.dto.PaymentCreateRequest;
import com.buu.order.dto.RemotePaymentDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.mapper.OrderItemMapper;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.OrderCreateService;
import com.buu.order.service.SeataTransactionRecordService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单创建服务实现
 * order-center 是 Seata TM，stock-center 和 pay-center 是 RM。
 */
@Service
public class OrderCreateServiceImpl implements OrderCreateService {

    private static final Logger log = LoggerFactory.getLogger(OrderCreateServiceImpl.class);
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final StockFeignClient stockFeignClient;
    private final PayFeignClient payFeignClient;
    private final SeataTransactionRecordService seataTransactionRecordService;

    public OrderCreateServiceImpl(OrderMainMapper orderMainMapper,
                                  OrderItemMapper orderItemMapper,
                                  ProductFeignClient productFeignClient,
                                  StockFeignClient stockFeignClient,
                                  PayFeignClient payFeignClient,
                                  SeataTransactionRecordService seataTransactionRecordService) {
        this.orderMainMapper = orderMainMapper;
        this.orderItemMapper = orderItemMapper;
        this.productFeignClient = productFeignClient;
        this.stockFeignClient = stockFeignClient;
        this.payFeignClient = payFeignClient;
        this.seataTransactionRecordService = seataTransactionRecordService;
    }

    /**
     * Seata 全局事务入口。
     * 任一远程分支失败时向外抛异常，TC 会通知已成功的 RM 通过 undo_log 回滚。
     */
    @Override
    @GlobalTransactional(name = "distribution-create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        validateRequest(request);
        String xid = RootContext.getXID();
        log.info("Seata global transaction started, xid={}", xid);
        String orderNo = null;
        boolean stockSucceeded = false;
        boolean paySucceeded = false;

        try {
            RemoteProductDTO product = requireSuccess(
                    productFeignClient.detail(request.getProductId()),
                    "商品服务查询失败"
            );

            LocalDateTime now = LocalDateTime.now();
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            orderNo = generateOrderNo(now);
            seataTransactionRecordService.recordStarted(orderNo, xid);

            OrderMain order = buildOrder(request, product, totalAmount, orderNo, now);
            insertRequired(orderMainMapper.insert(order), "订单主表创建失败");

            OrderItem item = buildOrderItem(order.getOrderId(), request, product, totalAmount, now);
            insertRequired(orderItemMapper.insert(item), "订单明细创建失败");

            R<Map<String, Object>> stockResult = stockFeignClient.deductStock(request.getProductId(), request.getQuantity());
            requireSuccess(stockResult, "库存扣减失败");
            stockSucceeded = true;
            seataTransactionRecordService.markStockSucceeded(orderNo, xid);

            PaymentCreateRequest paymentRequest = buildPaymentRequest(request, totalAmount, orderNo);
            RemotePaymentDTO payment = requireSuccess(payFeignClient.createPayment(paymentRequest), "支付单创建失败");
            paySucceeded = true;

            OrderMain paidOrder = new OrderMain();
            paidOrder.setOrderId(order.getOrderId());
            paidOrder.setOrderStatus("已支付");
            paidOrder.setLogisticsStatus("待发货");
            paidOrder.setCurrentLocation("北京顺义仓");
            paidOrder.setPayTime(LocalDateTime.now());
            updateRequired(orderMainMapper.updateById(paidOrder), "订单状态更新失败");
            seataTransactionRecordService.markCommitted(orderNo, xid);

            log.info("Seata global transaction completed, xid={}, orderNo={}", RootContext.getXID(), orderNo);
            return new OrderCreateResponse(
                    orderNo,
                    paidOrder.getOrderStatus(),
                    request.getProductId(),
                    product.getProductName(),
                    request.getQuantity(),
                    totalAmount,
                    "stock-center",
                    "pay-center",
                    xid,
                    payment
            );
        } catch (RuntimeException exception) {
            if (orderNo != null) {
                seataTransactionRecordService.markRolledBack(
                        orderNo,
                        xid,
                        stockSucceeded ? "ROLLED_BACK" : "FAILED",
                        paySucceeded ? "ROLLED_BACK" : "SKIPPED",
                        exception.getMessage()
                );
            }
            throw exception;
        }
    }

    private void validateRequest(OrderCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("创建订单请求不能为空");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }
    }

    private OrderMain buildOrder(OrderCreateRequest request,
                                 RemoteProductDTO product,
                                 BigDecimal totalAmount,
                                 String orderNo,
                                 LocalDateTime now) {
        OrderMain order = new OrderMain();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setProductName(product.getProductName());
        order.setProductNum(request.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("待支付");
        order.setReceiverName(defaultText(request.getReceiverName(), "实训用户"));
        order.setReceiverPhone(defaultText(request.getReceiverPhone(), "13800138000"));
        order.setReceiverAddress(defaultText(request.getReceiverAddress(), "北京市顺义区集散中心"));
        order.setLogisticsStatus("待支付");
        order.setCreateTime(now);
        order.setExpectArriveTime(now.plusDays(1));
        return order;
    }

    private OrderItem buildOrderItem(Long orderId,
                                     OrderCreateRequest request,
                                     RemoteProductDTO product,
                                     BigDecimal totalAmount,
                                     LocalDateTime now) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(request.getProductId());
        item.setProductName(product.getProductName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(request.getQuantity());
        item.setSubTotal(totalAmount);
        item.setCreateTime(now);
        return item;
    }

    private PaymentCreateRequest buildPaymentRequest(OrderCreateRequest request, BigDecimal totalAmount, String orderNo) {
        PaymentCreateRequest paymentRequest = new PaymentCreateRequest();
        paymentRequest.setOrderNo(orderNo);
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setPayAmount(totalAmount);
        paymentRequest.setPayMethod(defaultText(request.getPayMethod(), "支付宝"));
        paymentRequest.setSimulateFailure(Boolean.TRUE.equals(request.getSimulatePayFailure()));
        return paymentRequest;
    }

    private <T> T requireSuccess(R<T> response, String message) {
        if (response == null) {
            throw new IllegalStateException(message + "：无响应");
        }
        if (!Integer.valueOf(200).equals(response.getCode())) {
            throw new IllegalStateException(message + "：" + response.getMsg());
        }
        if (response.getData() == null) {
            throw new IllegalStateException(message + "：响应数据为空");
        }
        return response.getData();
    }

    private void insertRequired(int affectedRows, String message) {
        if (affectedRows <= 0) {
            throw new IllegalStateException(message);
        }
    }

    private void updateRequired(int affectedRows, String message) {
        if (affectedRows <= 0) {
            throw new IllegalStateException(message);
        }
    }

    private String generateOrderNo(LocalDateTime time) {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "DD" + ORDER_DATE_FORMATTER.format(time) + suffix;
    }

    private String defaultText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
