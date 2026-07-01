package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 经营分析驾驶舱响应
 * 聚合顶部订单指标、订单状态分布和最近七天订单趋势。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOperationsAnalysisResponse {

    private OrderSummaryDTO summary;
    private List<OrderStatusDistributionDTO> statusDistribution;
    private List<OrderTrendDTO> trend;
}
