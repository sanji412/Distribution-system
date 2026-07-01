package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单状态分布数据
 * 用于经营分析驾驶舱的订单状态占比图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDistributionDTO {

    private String label;
    private Long count;
    private Double percent;
    private String color;
}
