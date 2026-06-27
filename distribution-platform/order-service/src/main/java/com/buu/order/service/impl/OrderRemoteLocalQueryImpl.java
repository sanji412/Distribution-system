package com.buu.order.service.impl;

import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.mapper.OrderItemMapper;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.OrderRemoteLocalQuery;
import org.springframework.stereotype.Service;

/**
 * 订单本地数据查询实现
 * 负责从 order_db 读取订单主表和明细表数据。
 */
@Service
public class OrderRemoteLocalQueryImpl implements OrderRemoteLocalQuery {

    private final OrderMainMapper orderMainMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderRemoteLocalQueryImpl(OrderMainMapper orderMainMapper, OrderItemMapper orderItemMapper) {
        this.orderMainMapper = orderMainMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public OrderMain getOrderByOrderNo(String orderNo) {
        return orderMainMapper.selectByOrderNo(orderNo);
    }

    @Override
    public OrderItem getFirstItemByOrderId(Long orderId) {
        return orderItemMapper.selectFirstByOrderId(orderId);
    }
}
