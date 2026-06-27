package com.buu.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存中心远程库存响应
 * 字段与 stock-center 的 Stock 实体保持一致。
 */
@Data
public class RemoteStockDTO {

    private Long stockId;
    private Long productId;
    private Long warehouseId;
    private Integer stockNum;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
