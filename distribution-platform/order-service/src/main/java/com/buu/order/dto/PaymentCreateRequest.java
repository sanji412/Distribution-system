package com.buu.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付中心创建支付单请求
 * 字段名与 pay-center 的同名 DTO 保持一致，供 OpenFeign 序列化请求体。
 */
@Data
public class PaymentCreateRequest {

    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private String payMethod;
    private Boolean simulateFailure;
}
