package com.buu.order.service;

import com.buu.order.dto.OrderDashboardResponse;
import com.buu.order.dto.OrderListItemDTO;
import com.buu.order.dto.OrderSummaryDTO;
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
                "stock:8003",
                "pay:8006",
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
}
