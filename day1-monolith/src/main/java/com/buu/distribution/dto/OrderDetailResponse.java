package com.buu.distribution.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailResponse {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String productName;
    private Integer productNum;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String logisticsCompany;
    private String logisticsNo;
    private String logisticsStatus;
    private String currentLocation;
    private LocalDateTime expectArriveTime;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
}
