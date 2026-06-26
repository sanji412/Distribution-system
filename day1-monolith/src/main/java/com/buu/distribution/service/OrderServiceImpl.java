package com.buu.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.dto.CreateOrderRequest;
import com.buu.distribution.dto.DashboardResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private static final String ORDER_PENDING_PAY = "待支付";
    private static final String PAY_PENDING = "待支付";

    private final ProductMapper productMapper;
    private final StockMapper stockMapper;
    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;

    @Transactional(rollbackFor = Exception.class)
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        validateCreateRequest(request);

        Product product = productMapper.selectById(request.getProductId());
        if (product == null || Integer.valueOf(0).equals(product.getStatus())) {
            throw new BusinessException("商品不存在或已下架");
        }

        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, request.getProductId())
                .eq(Stock::getWarehouseId, request.getWarehouseId()));
        if (stock == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (stock.getStockNum() < request.getQuantity()) {
            throw new BusinessException("库存不足，当前库存：" + stock.getStockNum());
        }

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        String orderNo = buildOrderNo();

        stock.setStockNum(stock.getStockNum() - request.getQuantity());
        stockMapper.updateById(stock);

        OrderMain order = new OrderMain();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setProductName(product.getProductName());
        order.setProductNum(request.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(ORDER_PENDING_PAY);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setLogisticsStatus("待发货");
        order.setCreateTime(LocalDateTime.now());
        orderMainMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setProductId(product.getProductId());
        item.setProductName(product.getProductName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(request.getQuantity());
        item.setSubTotal(totalAmount);
        item.setCreateTime(LocalDateTime.now());
        orderItemMapper.insert(item);

        Payment payment = new Payment();
        payment.setPayNo(buildPayNo());
        payment.setOrderNo(orderNo);
        payment.setUserId(request.getUserId());
        payment.setPayAmount(totalAmount);
        payment.setPayStatus(PAY_PENDING);
        payment.setCreateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        return toDetail(order);
    }

    public List<OrderMain> listOrders() {
        return orderMainMapper.selectList(new LambdaQueryWrapper<OrderMain>().orderByDesc(OrderMain::getCreateTime));
    }

    public OrderDetailResponse getByOrderNo(String orderNo) {
        return toDetail(selectByOrderNo(orderNo));
    }

    public OrderDetailResponse updateStatus(UpdateOrderStatusRequest request) {
        OrderMain order = selectByOrderNo(request.getOrderNo());
        if (hasText(request.getOrderStatus())) {
            order.setOrderStatus(request.getOrderStatus());
            if ("已支付".equals(request.getOrderStatus())) {
                order.setPayTime(LocalDateTime.now());
            } else if ("已发货".equals(request.getOrderStatus())) {
                order.setDeliveryTime(LocalDateTime.now());
            } else if ("已完成".equals(request.getOrderStatus())) {
                order.setFinishTime(LocalDateTime.now());
            }
        }
        if (hasText(request.getLogisticsCompany())) {
            order.setLogisticsCompany(request.getLogisticsCompany());
        }
        if (hasText(request.getLogisticsNo())) {
            order.setLogisticsNo(request.getLogisticsNo());
        }
        if (hasText(request.getLogisticsStatus())) {
            order.setLogisticsStatus(request.getLogisticsStatus());
        }
        if (hasText(request.getCurrentLocation())) {
            order.setCurrentLocation(request.getCurrentLocation());
        }
        orderMainMapper.updateById(order);
        return toDetail(order);
    }

    public DashboardResponse dashboard() {
        List<OrderMain> orders = listOrders();
        DashboardResponse response = new DashboardResponse();
        response.setOrderCount((long) orders.size());
        response.setTotalAmount(orders.stream()
                .map(OrderMain::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setPendingShipmentCount(orders.stream()
                .filter(order -> "已支付".equals(order.getOrderStatus()))
                .count());
        response.setAbnormalOrderCount(orders.stream()
                .filter(order -> "库存不足".equals(order.getOrderStatus()))
                .count());

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (OrderMain order : orders) {
            distribution.merge(order.getOrderStatus(), 1L, Long::sum);
        }
        response.setOrderStatusDistribution(distribution);
        return response;
    }

    private void validateCreateRequest(CreateOrderRequest request) {
        if (request.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (request.getProductId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("仓库ID不能为空");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("购买数量必须大于0");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private OrderMain selectByOrderNo(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private OrderDetailResponse toDetail(OrderMain order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setProductName(order.getProductName());
        response.setProductNum(order.getProductNum());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setLogisticsCompany(order.getLogisticsCompany());
        response.setLogisticsNo(order.getLogisticsNo());
        response.setLogisticsStatus(order.getLogisticsStatus());
        response.setCurrentLocation(order.getCurrentLocation());
        response.setExpectArriveTime(order.getExpectArriveTime());
        response.setCreateTime(order.getCreateTime());
        response.setPayTime(order.getPayTime());
        response.setDeliveryTime(order.getDeliveryTime());
        response.setFinishTime(order.getFinishTime());
        return response;
    }

    private String buildOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DD" + date + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String buildPayNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAY" + date + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
