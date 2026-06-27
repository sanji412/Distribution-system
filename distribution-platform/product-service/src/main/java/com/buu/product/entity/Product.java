package com.buu.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 * 保存商品档案、价格、SKU、安全库存和上下架状态。
 */
@Data
@TableName("product")
public class Product {

    @TableId(value = "product_id", type = IdType.AUTO)
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
