package com.buu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.order.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

/**
 * 订单明细 Mapper
 * 负责订单明细表的基础 CRUD 操作。
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 查询订单的第一条明细
     *
     * @param orderId 订单主键 ID
     * @return 订单明细记录
     */
    OrderItem selectFirstByOrderId(@Param("orderId") Long orderId);
}
