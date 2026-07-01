package com.buu.gateway.service;

import com.buu.gateway.dto.GovernanceOverviewResponse;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GovernanceOverviewServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-30T02:05:00Z"),
            ZoneId.of("Asia/Shanghai")
    );

    @Test
    void buildsGovernanceOverviewFromDiscoveryInstancesAndGatewayRoutes() {
        DiscoveryClient discoveryClient = new FakeDiscoveryClient(Map.of(
                "order-center", List.of(instance("order-center", "127.0.0.1", 8005)),
                "stock-center", List.of(
                        instance("stock-center", "127.0.0.1", 8003),
                        instance("stock-center", "127.0.0.1", 8004)
                )
        ));
        RouteDefinitionLocator routeLocator = () -> Flux.just(route("order_route", "lb://order-center", "Path=/api/order/**"));
        GovernanceOverviewService service = new GovernanceOverviewService(
                discoveryClient,
                routeLocator,
                (host, port) -> 11L,
                FIXED_CLOCK
        );

        GovernanceOverviewResponse response = service.getOverview();

        assertThat(response.metrics()).extracting(GovernanceOverviewResponse.MetricItem::value)
                .containsExactly("2", "3", "1", "0");
        assertThat(response.instances()).hasSize(2);
        assertThat(response.instances().getFirst().name()).isEqualTo("stock-center");
        assertThat(response.instances().getFirst().desc()).contains("8003/8004").contains("2实例");
        assertThat(response.instances().getFirst().latency()).isEqualTo("11ms");
        assertThat(response.logs()).anySatisfy(line ->
                assertThat(line).contains("[10:05:00]").contains("Service registered: stock-center 127.0.0.1:8003")
        );
        assertThat(response.routes()).contains(
                "spring.cloud.gateway.routes[0].id=order_route",
                "spring.cloud.gateway.routes[0].uri=lb://order-center",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/order/**"
        );
    }

    @Test
    void reportsEmptyRegistryAsOfflineStatus() {
        GovernanceOverviewService service = new GovernanceOverviewService(
                new FakeDiscoveryClient(Map.of()),
                Flux::empty,
                (host, port) -> null,
                FIXED_CLOCK
        );

        GovernanceOverviewResponse response = service.getOverview();

        assertThat(response.status()).isEqualTo("未注册");
        assertThat(response.metrics().getFirst().hint()).isEqualTo("等待服务注册");
        assertThat(response.logs()).contains("[10:05:00] [WARN] Nacos registry has no active service instances");
        assertThat(response.instances()).isEmpty();
    }

    private static RouteDefinition route(String id, String uri, String predicate) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(id);
        routeDefinition.setUri(URI.create(uri));
        routeDefinition.setPredicates(List.of(new PredicateDefinition(predicate)));
        return routeDefinition;
    }

    private static ServiceInstance instance(String serviceId, String host, int port) {
        return new ServiceInstance() {
            @Override
            public String getServiceId() {
                return serviceId;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public URI getUri() {
                return URI.create("http://" + host + ":" + port);
            }

            @Override
            public Map<String, String> getMetadata() {
                return Map.of();
            }
        };
    }

    private record FakeDiscoveryClient(Map<String, List<ServiceInstance>> instances) implements DiscoveryClient {

        @Override
        public String description() {
            return "fake discovery";
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return instances.getOrDefault(serviceId, List.of());
        }

        @Override
        public List<String> getServices() {
            return instances.keySet().stream().toList();
        }
    }
}
