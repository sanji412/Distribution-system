package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
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
