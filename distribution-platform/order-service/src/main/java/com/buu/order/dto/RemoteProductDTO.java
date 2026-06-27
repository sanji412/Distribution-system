package com.buu.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品中心远程商品响应
 * 字段与 product-center 的 Product 实体保持一致。
 */
@Data
public class RemoteProductDTO {

    private Long productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private String skuCode;
    private String description;
    private Integer status;
    private Integer safeStock;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
