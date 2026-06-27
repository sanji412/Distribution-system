package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单列表行数据
 * 面向订单履约页面，聚合订单展示字段和模拟跨服务调用标签。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemDTO {

    private String orderNo;
    private String productName;
    private Integer productNum;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String stockService;
    private String payService;
    private String orderTime;
}
