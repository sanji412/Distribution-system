package com.buu.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体
 * 保存订单中的商品快照信息，便于后续扩展多商品订单。
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subTotal;
    private LocalDateTime createTime;
}
