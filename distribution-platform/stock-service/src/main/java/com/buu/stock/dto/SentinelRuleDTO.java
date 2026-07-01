package com.buu.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sentinel rule display row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentinelRuleDTO {

    private String resource;
    private Double threshold;
    private Long qps;
    private Long blocked;
    private String status;
    private String strategy;
}
