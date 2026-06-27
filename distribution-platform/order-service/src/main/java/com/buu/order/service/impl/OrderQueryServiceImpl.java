package com.buu.order.service.impl;

import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderSummaryDTO;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.OrderQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单查询服务实现
 * 从订单库聚合履约看板指标，并读取订单列表展示数据。
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderMainMapper orderMainMapper;

    public OrderQueryServiceImpl(OrderMainMapper orderMainMapper) {
        this.orderMainMapper = orderMainMapper;
    }

    /**
     * 查询订单履约看板数据
     *
     * @param bizDate 业务日期
     * @return 汇总指标和订单列表
     */
    @Override
    public OrderDashboardResponse getDashboard(LocalDate bizDate) {
        return new OrderDashboardResponse(getSummary(bizDate), listOrders());
    }

    /**
     * 查询指定日期的订单汇总指标
     *
     * @param bizDate 业务日期
     * @return 今日订单、成交额、待发货和异常订单数据
     */
    @Override
    public OrderSummaryDTO getSummary(LocalDate bizDate) {
        LocalDateTime startTime = bizDate.atStartOfDay();
        LocalDateTime endTime = bizDate.plusDays(1).atStartOfDay();
        OrderSummaryDTO summary = orderMainMapper.selectTodaySummary(startTime, endTime);
        return summary == null ? emptySummary() : summary;
    }

    /**
     * 查询订单履约列表
     *
     * @return 订单列表
     */
    @Override
    public List<OrderListItemDTO> listOrders() {
        return orderMainMapper.selectOrderList();
    }

    private OrderSummaryDTO emptySummary() {
        return new OrderSummaryDTO(0L, BigDecimal.ZERO, 0L, 0L);
    }
}
