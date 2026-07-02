package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单实体类
 * 对应数据库表 t_order
 */
@Data
@TableName("t_order")
public class Order {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单总金额
     */
    private BigDecimal money;

    /**
     * 订单状态：0-创建中，1-已完成
     */
    private Integer status;
}