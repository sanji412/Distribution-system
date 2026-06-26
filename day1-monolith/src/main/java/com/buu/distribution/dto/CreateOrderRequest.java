package com.buu.distribution.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {

    private Long userId;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}
