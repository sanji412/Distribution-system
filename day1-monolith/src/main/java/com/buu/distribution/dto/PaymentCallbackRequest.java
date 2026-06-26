package com.buu.distribution.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {

    private String orderNo;
    private String payStatus;
    private String payMethod;
    private String callbackContent;
}
