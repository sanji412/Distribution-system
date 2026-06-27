package com.buu.order.service;

import com.buu.order.dto.OrderRemoteDetailDTO;

/**
 * 订单跨服务查询服务
 * 负责通过 OpenFeign 聚合订单、商品、库存和支付信息。
 */
public interface OrderRemoteQueryService {

    /**
     * 查询订单跨服务详情
     *
     * @param orderNo 订单编号
     * @return 聚合后的订单跨服务详情
     */
    OrderRemoteDetailDTO getRemoteDetail(String orderNo);
}
