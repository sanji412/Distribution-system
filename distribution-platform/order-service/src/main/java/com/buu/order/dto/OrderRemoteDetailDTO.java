package com.buu.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单跨服务详情响应
 * 用于展示 order-center 通过 OpenFeign 调用商品、库存和支付中心后的聚合结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRemoteDetailDTO {

    private String orderNo;
    private String productName;
    private Integer productNum;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String productService;
    private String stockService;
    private String payService;
    private RemoteProductDTO product;
    private List<RemoteStockDTO> stocks;
    private RemotePaymentDTO payment;
}
