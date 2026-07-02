package com.buu.order.service;

import com.buu.order.dto.SeataFlowResponse;

/**
 * Seata 演示数据服务
 * 根据真实订单状态生成前端事务链路展示内容。
 */
public interface SeataDemoService {

    /**
     * 查询事务链路展示数据
     *
     * @return Seata 事务链路展示数据
     */
    SeataFlowResponse getFlow();
}
