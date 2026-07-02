package com.buu.order.dto;

import lombok.Data;

/**
 * 创建订单请求
 * 作为 Seata 全局事务入口的请求参数。
 */
@Data
public class OrderCreateRequest {

    private Long userId;
    private Long productId;
    private Integer quantity;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String payMethod;
    private Boolean simulatePayFailure;
}
