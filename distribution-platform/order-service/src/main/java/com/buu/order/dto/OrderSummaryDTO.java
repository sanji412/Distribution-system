package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单履约汇总数据
 * 用于展示今日订单、今日成交额、待发货和异常订单四个指标。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

    private Long todayOrderCount;
    private BigDecimal todayTradeAmount;
    private Long pendingDeliveryCount;
    private Long exceptionOrderCount;
}
