package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建订单响应
 * 用于展示 Seata 下单链路的执行结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResponse {

    private String orderNo;
    private String orderStatus;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String stockService;
    private String payService;
    private String xid;
    private RemotePaymentDTO payment;
}
