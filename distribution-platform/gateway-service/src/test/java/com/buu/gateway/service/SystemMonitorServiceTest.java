package com.buu.gateway.service;

import com.buu.gateway.dto.GovernanceOverviewResponse;
import com.buu.gateway.dto.SystemMonitorResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SystemMonitorServiceTest {

    @Test
    void buildsSystemMonitorFromRuntimeAndGovernanceOverview() {
        GovernanceOverviewService governanceOverviewService = mock(GovernanceOverviewService.class);
        given(governanceOverviewService.getOverview()).willReturn(new GovernanceOverviewResponse(
                "运行中",
                List.of(
                        new GovernanceOverviewResponse.MetricItem("微服务数", "6", "success", "全部健康"),
                        new GovernanceOverviewResponse.MetricItem("服务实例", "6", "primary", "来自Nacos注册中心"),
                        new GovernanceOverviewResponse.MetricItem("GATEWAY路由", "5", "warning", "已配置"),
                        new GovernanceOverviewResponse.MetricItem("熔断降级", "0", "danger", "当前正常")
                ),
                List.of(),
                List.of(),
                List.of("spring.cloud.gateway.routes[0].id=order_route")
        ));
        Clock clock = Clock.fixed(Instant.parse("2026-07-01T04:00:00Z"), ZoneId.of("Asia/Shanghai"));

        SystemMonitorService service = new SystemMonitorService(governanceOverviewService, clock);
        SystemMonitorResponse response = service.getMonitor();

        assertThat(response.cards()).extracting(SystemMonitorResponse.MonitorCard::title)
                .contains("SPRINGBOOT 版本", "JDK 版本", "NACOS 服务数", "GATEWAY 路由");
        assertThat(response.rows()).anySatisfy(row -> {
            assertThat(row.metric()).isEqualTo("Nacos服务数");
            assertThat(row.value()).isEqualTo("6");
        });
        assertThat(response.rows()).anySatisfy(row -> {
            assertThat(row.metric()).isEqualTo("系统状态");
            assertThat(row.value()).isEqualTo("运行中");
        });
    }
}
