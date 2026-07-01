package com.buu.gateway.service;

import com.buu.gateway.dto.GovernanceOverviewResponse;
import com.buu.gateway.dto.SystemMonitorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Builds system monitor data from the current JVM and service governance snapshot.
 */
@Service
public class SystemMonitorService {

    private final GovernanceOverviewService governanceOverviewService;
    private final Clock clock;
    private final Instant startedAt;

    @Autowired
    public SystemMonitorService(GovernanceOverviewService governanceOverviewService) {
        this(governanceOverviewService, Clock.systemDefaultZone());
    }

    SystemMonitorService(GovernanceOverviewService governanceOverviewService, Clock clock) {
        this.governanceOverviewService = governanceOverviewService;
        this.clock = clock;
        this.startedAt = clock.instant();
    }

    /**
     * Returns system monitor data used by the Vue system page.
     *
     * @return system monitor response
     */
    public SystemMonitorResponse getMonitor() {
        GovernanceOverviewResponse overview = governanceOverviewService.getOverview();
        String serviceCount = metricValue(overview, "微服务数");
        String instanceCount = metricValue(overview, "服务实例");
        String routeCount = metricValue(overview, "GATEWAY路由");
        String abnormalCount = metricValue(overview, "熔断降级");
        String javaVersion = String.valueOf(Runtime.version().feature());
        String bootVersion = nullToUnknown(SpringBootVersion.getVersion());

        return new SystemMonitorResponse(
                List.of(
                        new SystemMonitorResponse.MonitorCard("SPRINGBOOT 版本", bootVersion, "primary"),
                        new SystemMonitorResponse.MonitorCard("JDK 版本", javaVersion + " LTS", "primary"),
                        new SystemMonitorResponse.MonitorCard("NACOS 服务数", serviceCount, "success"),
                        new SystemMonitorResponse.MonitorCard("GATEWAY 路由", routeCount, "warning"),
                        new SystemMonitorResponse.MonitorCard("服务实例", instanceCount, "success"),
                        new SystemMonitorResponse.MonitorCard("运行时长", formatUptime(), "primary")
                ),
                List.of(
                        new SystemMonitorResponse.MonitorRow("SpringBoot版本", bootVersion, "Spring Framework " + nullToUnknown(SpringVersion.getVersion())),
                        new SystemMonitorResponse.MonitorRow("JDK版本", javaVersion, nullToUnknown(System.getProperty("java.vendor"))),
                        new SystemMonitorResponse.MonitorRow("Nacos服务数", serviceCount, "来自Nacos注册中心的当前服务分组数"),
                        new SystemMonitorResponse.MonitorRow("服务实例", instanceCount, "当前已注册且可发现的实例数"),
                        new SystemMonitorResponse.MonitorRow("Gateway路由", routeCount, "当前Gateway已加载路由数"),
                        new SystemMonitorResponse.MonitorRow("不可达服务组", abnormalCount, "TCP探测不可达的服务分组数量"),
                        new SystemMonitorResponse.MonitorRow("系统状态", overview.status(), "Gateway实时聚合的治理状态"),
                        new SystemMonitorResponse.MonitorRow("JVM进程", ManagementFactory.getRuntimeMXBean().getName(), "当前Gateway运行进程")
                ),
                List.of(
                        "DDD服务拆分：按业务领域拆分微服务边界",
                        "Nacos注册发现：服务注册、实例心跳、配置推送",
                        "OpenFeign：声明式远程调用与服务间协作",
                        "Gateway网关：统一入口、路由转发、流量统计",
                        "Sentinel限流降级：QPS限流、热点参数、降级回调",
                        "Seata AT分布式事务：undo_log 回滚机制",
                        "DeepSeek AI接入：Prompt工程与业务问答"
                )
        );
    }

    private String metricValue(GovernanceOverviewResponse overview, String title) {
        return overview.metrics().stream()
                .filter(metric -> title.equals(metric.title()))
                .findFirst()
                .map(GovernanceOverviewResponse.MetricItem::value)
                .orElse("0");
    }

    private String formatUptime() {
        Duration duration = Duration.between(startedAt, clock.instant());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    private String nullToUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
