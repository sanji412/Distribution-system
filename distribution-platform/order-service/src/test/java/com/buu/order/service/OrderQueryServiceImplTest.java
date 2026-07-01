package com.buu.order.service;

import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderOperationsAnalysisResponse;
import com.buu.order.dto.OrderStatusDistributionDTO;
import com.buu.order.dto.OrderSummaryDTO;
import com.buu.order.dto.OrderTrendDTO;
import com.buu.order.mapper.OrderMainMapper;
import com.buu.order.service.impl.OrderQueryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceImplTest {

    @Mock
    private OrderMainMapper orderMainMapper;

    @Test
    void getDashboardReturnsTodaySummaryAndOrderRows() {
        LocalDate today = LocalDate.of(2026, 6, 26);
        LocalDateTime dayStart = LocalDateTime.of(2026, 6, 26, 0, 0);
        LocalDateTime dayEnd = LocalDateTime.of(2026, 6, 27, 0, 0);

        OrderSummaryDTO summary = new OrderSummaryDTO(1280L, new BigDecimal("128000.00"), 23L, 5L);
        OrderListItemDTO row = new OrderListItemDTO(
                "DD202505190001",
                "机械键盘-Keychron K3",
                2,
                new BigDecimal("736.00"),
                "已支付",
                "stock-center",
                "pay-center",
                "09:30"
        );

        given(orderMainMapper.selectTodaySummary(dayStart, dayEnd)).willReturn(summary);
        given(orderMainMapper.selectOrderList()).willReturn(List.of(row));

        OrderQueryServiceImpl service = new OrderQueryServiceImpl(orderMainMapper);
        OrderDashboardResponse dashboard = service.getDashboard(today);

        assertThat(dashboard.getSummary().getTodayOrderCount()).isEqualTo(1280L);
        assertThat(dashboard.getSummary().getTodayTradeAmount()).isEqualByComparingTo("128000.00");
        assertThat(dashboard.getSummary().getPendingDeliveryCount()).isEqualTo(23L);
        assertThat(dashboard.getSummary().getExceptionOrderCount()).isEqualTo(5L);
        assertThat(dashboard.getOrders()).hasSize(1);
        assertThat(dashboard.getOrders().get(0).getOrderNo()).isEqualTo("DD202505190001");

        verify(orderMainMapper).selectTodaySummary(dayStart, dayEnd);
        verify(orderMainMapper).selectOrderList();
    }

    @Test
    void getOperationsAnalysisReturnsSummaryStatusDistributionAndSevenDayTrend() {
        LocalDate today = LocalDate.of(2026, 7, 1);
        LocalDateTime dayStart = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime dayEnd = LocalDateTime.of(2026, 7, 2, 0, 0);
        LocalDateTime trendStart = LocalDateTime.of(2026, 6, 25, 0, 0);

        OrderSummaryDTO summary = new OrderSummaryDTO(10L, new BigDecimal("1024.00"), 3L, 1L);
        List<OrderStatusDistributionDTO> statusRows = List.of(
                new OrderStatusDistributionDTO("已支付", 5L, 0D, "#5b8def"),
                new OrderStatusDistributionDTO("已完成", 3L, 0D, "#73bf69"),
                new OrderStatusDistributionDTO("库存不足", 2L, 0D, "#f0445e")
        );
        List<OrderTrendDTO> trendRows = List.of(
                new OrderTrendDTO("06/30", 4L, new BigDecimal("400.00")),
                new OrderTrendDTO("07/01", 6L, new BigDecimal("624.00"))
        );

        given(orderMainMapper.selectTodaySummary(dayStart, dayEnd)).willReturn(summary);
        given(orderMainMapper.selectStatusDistribution(dayStart, dayEnd)).willReturn(statusRows);
        given(orderMainMapper.selectSevenDayTrend(trendStart, dayEnd)).willReturn(trendRows);

        OrderQueryServiceImpl service = new OrderQueryServiceImpl(orderMainMapper);
        OrderOperationsAnalysisResponse response = service.getOperationsAnalysis(today);

        assertThat(response.getSummary().getTodayOrderCount()).isEqualTo(10L);
        assertThat(response.getStatusDistribution()).extracting(OrderStatusDistributionDTO::getLabel)
                .containsExactly("已支付", "已完成", "库存不足");
        assertThat(response.getStatusDistribution()).extracting(OrderStatusDistributionDTO::getPercent)
                .containsExactly(50D, 30D, 20D);
        assertThat(response.getTrend()).extracting(OrderTrendDTO::getDate)
                .containsExactly("06/25", "06/26", "06/27", "06/28", "06/29", "06/30", "07/01");
        assertThat(response.getTrend()).extracting(OrderTrendDTO::getOrderCount)
                .containsExactly(0L, 0L, 0L, 0L, 0L, 4L, 6L);

        verify(orderMainMapper).selectTodaySummary(dayStart, dayEnd);
        verify(orderMainMapper).selectStatusDistribution(dayStart, dayEnd);
        verify(orderMainMapper).selectSevenDayTrend(trendStart, dayEnd);
    }
}
