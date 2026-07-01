package com.buu.gateway.service;

import com.buu.gateway.dto.GovernanceOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregates live Nacos discovery and Gateway route data for the service governance page.
 */
@Service
public class GovernanceOverviewService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final List<String> SERVICE_ORDER = List.of(
            "user-center",
            "product-center",
            "stock-center",
            "order-center",
            "pay-center",
            "gateway-service"
    );
    private static final Map<String, String> SERVICE_LABELS = Map.of(
            "user-center", "用户中心",
            "product-center", "商品中心",
            "stock-center", "库存中心",
            "order-center", "订单中心",
            "pay-center", "支付中心",
            "gateway-service", "网关服务"
    );

    private final DiscoveryClient discoveryClient;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final LatencyProbe latencyProbe;
    private final Clock clock;

    @Autowired
    public GovernanceOverviewService(
            DiscoveryClient discoveryClient,
            RouteDefinitionLocator routeDefinitionLocator,
            LatencyProbe latencyProbe
    ) {
        this(discoveryClient, routeDefinitionLocator, latencyProbe, Clock.systemDefaultZone());
    }

    GovernanceOverviewService(
            DiscoveryClient discoveryClient,
            RouteDefinitionLocator routeDefinitionLocator,
            LatencyProbe latencyProbe,
            Clock clock
    ) {
        this.discoveryClient = discoveryClient;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.latencyProbe = latencyProbe;
        this.clock = clock;
    }

    /**
     * Builds a governance overview snapshot from live registry and route data.
     *
     * @return governance page response DTO
     */
    public GovernanceOverviewResponse getOverview() {
        List<ServiceSnapshot> services = activeServiceSnapshots();
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(2));
        if (routes == null) {
            routes = List.of();
        }
        routes = routes.stream()
                .sorted(Comparator.comparing(RouteDefinition::getId))
                .toList();

        int serviceCount = services.size();
        int instanceCount = services.stream().mapToInt(snapshot -> snapshot.instances().size()).sum();
        long unhealthyCount = services.stream().filter(snapshot -> !snapshot.healthy()).count();
        String status = serviceCount == 0 ? "未注册" : (unhealthyCount == 0 ? "运行中" : "部分异常");

        return new GovernanceOverviewResponse(
                status,
                buildMetrics(serviceCount, instanceCount, routes.size(), unhealthyCount),
                buildRegistryLogs(services, unhealthyCount),
                buildInstanceItems(services),
                buildRouteLines(routes)
        );
    }

    private List<ServiceSnapshot> activeServiceSnapshots() {
        return discoveryClient.getServices().stream()
                .map(serviceName -> new ServiceSnapshot(serviceName, discoveryClient.getInstances(serviceName)))
                .filter(snapshot -> !snapshot.instances().isEmpty())
                .sorted(Comparator.comparingInt(snapshot -> serviceIndex(snapshot.serviceName())))
                .map(this::withLatency)
                .toList();
    }

    private ServiceSnapshot withLatency(ServiceSnapshot snapshot) {
        List<Long> latencies = snapshot.instances().stream()
                .map(instance -> latencyProbe.probe(instance.getHost(), instance.getPort()))
                .filter(Objects::nonNull)
                .toList();
        boolean healthy = latencies.size() == snapshot.instances().size();
        Long averageLatency = latencies.isEmpty()
                ? null
                : Math.round(latencies.stream().mapToLong(Long::longValue).average().orElse(0D));
        return new ServiceSnapshot(snapshot.serviceName(), snapshot.instances(), averageLatency, healthy);
    }

    private List<GovernanceOverviewResponse.MetricItem> buildMetrics(
            int serviceCount,
            int instanceCount,
            int routeCount,
            long unhealthyCount
    ) {
        boolean hasServices = serviceCount > 0;
        String healthHint = hasServices
                ? (unhealthyCount == 0 ? "全部健康" : "部分实例不可达")
                : "等待服务注册";

        return List.of(
                new GovernanceOverviewResponse.MetricItem("微服务数", String.valueOf(serviceCount), "success", healthHint),
                new GovernanceOverviewResponse.MetricItem("服务实例", String.valueOf(instanceCount), "primary", "来自Nacos注册中心"),
                new GovernanceOverviewResponse.MetricItem("GATEWAY路由", String.valueOf(routeCount), "warning", "已配置"),
                new GovernanceOverviewResponse.MetricItem("熔断降级", String.valueOf(unhealthyCount), "danger", unhealthyCount == 0 ? "当前正常" : "需排查")
        );
    }

    private List<String> buildRegistryLogs(List<ServiceSnapshot> services, long unhealthyCount) {
        String now = "[" + LocalTime.now(clock).format(TIME_FORMATTER) + "]";
        List<String> logs = new ArrayList<>();

        if (services.isEmpty()) {
            logs.add(now + " [WARN] Nacos registry has no active service instances");
            return logs;
        }

        logs.add(now + " [INFO] Nacos discovery snapshot loaded successfully");
        services.forEach(snapshot -> snapshot.instances().forEach(instance ->
                logs.add(now + " [INFO] Service registered: "
                        + snapshot.serviceName()
                        + " "
                        + instance.getHost()
                        + ":"
                        + instance.getPort())
        ));
        String heartbeatLevel = unhealthyCount == 0 ? "INFO" : "WARN";
        String heartbeatText = unhealthyCount == 0
                ? "Heartbeat check: all registered services healthy"
                : "Heartbeat check: " + unhealthyCount + " registered service group(s) unreachable";
        logs.add(now + " [" + heartbeatLevel + "] " + heartbeatText);
        return logs;
    }

    private List<GovernanceOverviewResponse.InstanceItem> buildInstanceItems(List<ServiceSnapshot> services) {
        return services.stream()
                .map(snapshot -> new GovernanceOverviewResponse.InstanceItem(
                        snapshot.serviceName(),
                        serviceDescription(snapshot),
                        snapshot.averageLatency() == null ? "离线" : snapshot.averageLatency() + "ms",
                        snapshot.healthy()
                ))
                .toList();
    }

    private String serviceDescription(ServiceSnapshot snapshot) {
        String label = SERVICE_LABELS.getOrDefault(snapshot.serviceName(), snapshot.serviceName());
        String ports = snapshot.instances().stream()
                .map(instance -> String.valueOf(instance.getPort()))
                .distinct()
                .reduce((left, right) -> left + "/" + right)
                .orElse("-");
        String suffix = "gateway-service".equals(snapshot.serviceName()) ? "统一入口" : snapshot.instances().size() + "实例";
        return label + " · " + ports + " · " + suffix;
    }

    private List<String> buildRouteLines(List<RouteDefinition> routes) {
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < routes.size(); index++) {
            RouteDefinition route = routes.get(index);
            String prefix = "spring.cloud.gateway.routes[" + index + "]";
            lines.add(prefix + ".id=" + route.getId());
            lines.add(prefix + ".uri=" + route.getUri());
            List<PredicateDefinition> predicates = route.getPredicates();
            for (int predicateIndex = 0; predicateIndex < predicates.size(); predicateIndex++) {
                lines.add(prefix + ".predicates[" + predicateIndex + "]=" + formatPredicate(predicates.get(predicateIndex)));
            }
        }
        return lines;
    }

    private String formatPredicate(PredicateDefinition predicate) {
        String args = String.join(",", predicate.getArgs().values());
        return predicate.getName() + "=" + args;
    }

    private int serviceIndex(String serviceName) {
        int index = SERVICE_ORDER.indexOf(serviceName);
        return index >= 0 ? index : SERVICE_ORDER.size();
    }

    private record ServiceSnapshot(
            String serviceName,
            List<ServiceInstance> instances,
            Long averageLatency,
            boolean healthy
    ) {

        private ServiceSnapshot(String serviceName, List<ServiceInstance> instances) {
            this(serviceName, instances, null, false);
        }
    }
}
