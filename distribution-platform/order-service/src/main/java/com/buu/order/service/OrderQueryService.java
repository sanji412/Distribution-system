package com.buu.order.service;

import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderSummaryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单查询服务
 * 提供订单履约页面所需的指标和列表查询能力。
 */
public interface OrderQueryService {

    /**
     * 查询订单履约看板数据
     *
     * @param bizDate 业务日期
     * @return 汇总指标和订单列表
     */
    OrderDashboardResponse getDashboard(LocalDate bizDate);

    /**
     * 查询指定日期的订单汇总指标
     *
     * @param bizDate 业务日期
     * @return 今日订单、成交额、待发货和异常订单数据
     */
    OrderSummaryDTO getSummary(LocalDate bizDate);

    /**
     * 查询订单履约列表
     *
     * @return 订单列表
     */
    List<OrderListItemDTO> listOrders();
}
