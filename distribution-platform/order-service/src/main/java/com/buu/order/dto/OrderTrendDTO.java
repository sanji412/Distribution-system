package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单趋势数据
 * 用于展示最近七天订单量和成交额变化。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrendDTO {

    private String date;
    private Long orderCount;
    private BigDecimal tradeAmount;
}
