package com.buu.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付单请求
 * 由 order-center 通过 OpenFeign 发起，是 Seata 支付分支事务的入口参数。
 */
@Data
public class PaymentCreateRequest {

    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private String payMethod;
    private Boolean simulateFailure;
}
