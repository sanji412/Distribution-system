package com.buu.order.service.impl;

import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderOperationsAnalysisResponse;
import com.buu.order.dto.OrderStatusDistributionDTO;
import com.buu.order.dto.OrderSummaryDTO;
import com.buu.order.dto.OrderTrendDTO;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.OrderQueryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单查询服务实现
 * 从订单库聚合履约看板指标，并读取订单列表展示数据。
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final DateTimeFormatter TREND_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    private static final Map<String, String> STATUS_COLORS = Map.of(
            "已支付", "#5b8def",
            "已完成", "#73bf69",
            "待支付", "#f1c40f",
            "已发货", "#26c6da",
            "库存不足", "#f0445e"
    );

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
     * 查询经营分析驾驶舱数据
     *
     * @param bizDate 业务日期
     * @return 今日指标、状态分布和七天趋势
     */
    @Override
    public OrderOperationsAnalysisResponse getOperationsAnalysis(LocalDate bizDate) {
        LocalDateTime startTime = bizDate.atStartOfDay();
        LocalDateTime endTime = bizDate.plusDays(1).atStartOfDay();
        LocalDate trendStartDate = bizDate.minusDays(6);
        LocalDateTime trendStartTime = trendStartDate.atStartOfDay();

        OrderSummaryDTO summary = getSummary(bizDate);
        List<OrderStatusDistributionDTO> statusDistribution = withPercent(
                orderMainMapper.selectStatusDistribution(startTime, endTime),
                summary.getTodayOrderCount()
        );
        List<OrderTrendDTO> trend = fillSevenDayTrend(
                trendStartDate,
                orderMainMapper.selectSevenDayTrend(trendStartTime, endTime)
        );

        return new OrderOperationsAnalysisResponse(summary, statusDistribution, trend);
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

    private List<OrderStatusDistributionDTO> withPercent(List<OrderStatusDistributionDTO> rows, Long totalCount) {
        long total = Optional.ofNullable(totalCount).orElse(0L);
        return Optional.ofNullable(rows).orElse(List.of()).stream()
                .map(row -> {
                    long count = Optional.ofNullable(row.getCount()).orElse(0L);
                    double percent = total == 0 ? 0D : Math.round(count * 1000D / total) / 10D;
                    String color = STATUS_COLORS.getOrDefault(row.getLabel(), "#8d929b");
                    return new OrderStatusDistributionDTO(row.getLabel(), count, percent, color);
                })
                .toList();
    }

    private List<OrderTrendDTO> fillSevenDayTrend(LocalDate startDate, List<OrderTrendDTO> rows) {
        Map<String, OrderTrendDTO> trendMap = new LinkedHashMap<>();
        Optional.ofNullable(rows).orElse(List.of())
                .forEach(row -> trendMap.put(row.getDate(), row));

        List<OrderTrendDTO> trend = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            String label = startDate.plusDays(offset).format(TREND_DATE_FORMATTER);
            OrderTrendDTO row = trendMap.get(label);
            if (row == null) {
                trend.add(new OrderTrendDTO(label, 0L, BigDecimal.ZERO));
            } else {
                trend.add(new OrderTrendDTO(
                        label,
                        Optional.ofNullable(row.getOrderCount()).orElse(0L),
                        Optional.ofNullable(row.getTradeAmount()).orElse(BigDecimal.ZERO)
                ));
            }
        }
        return trend;
    }
}
