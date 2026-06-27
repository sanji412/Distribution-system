package com.buu.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付中心远程支付响应
 * 字段与 pay-center 的 Payment 实体保持一致。
 */
@Data
public class RemotePaymentDTO {

    private Long payId;
    private String payNo;
    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private String payMethod;
    private String payStatus;
    private String callbackContent;
    private LocalDateTime callbackTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
