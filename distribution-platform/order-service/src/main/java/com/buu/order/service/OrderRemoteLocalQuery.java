package com.buu.order.service;

import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;

/**
 * 订单本地数据查询接口
 * 将订单库读取逻辑与远程服务聚合逻辑隔离，方便独立测试。
 */
public interface OrderRemoteLocalQuery {

    /**
     * 根据订单编号查询订单主表
     *
     * @param orderNo 订单编号
     * @return 订单主表记录
     */
    OrderMain getOrderByOrderNo(String orderNo);

    /**
     * 查询订单的第一条明细
     *
     * @param orderId 订单主键 ID
     * @return 订单明细记录
     */
    OrderItem getFirstItemByOrderId(Long orderId);
}
