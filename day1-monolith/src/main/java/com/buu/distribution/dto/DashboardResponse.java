package com.buu.distribution.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DashboardResponse {

    private Long orderCount;
    private BigDecimal totalAmount;
    private Long pendingShipmentCount;
    private Long abnormalOrderCount;
    private Map<String, Long> orderStatusDistribution;
}
