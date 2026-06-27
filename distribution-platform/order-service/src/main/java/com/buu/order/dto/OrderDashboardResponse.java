package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单履约看板响应
 * 同时返回顶部汇总卡片和下方订单列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDashboardResponse {

    private OrderSummaryDTO summary;
    private List<OrderListItemDTO> orders;
}
