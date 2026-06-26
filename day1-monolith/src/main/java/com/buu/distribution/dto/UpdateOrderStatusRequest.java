package com.buu.distribution.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    private String orderNo;
    private String orderStatus;
    private String logisticsCompany;
    private String logisticsNo;
    private String logisticsStatus;
    private String currentLocation;
}
