package com.buu.gateway.service;

import com.buu.gateway.dto.GatewayTrafficResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Counts requests passing through Gateway and groups them by routed service.
 */
@Service
public class GatewayTrafficService {

    private static final String GATEWAY = "Gateway";
    private static final List<ServiceRoute> SERVICE_ROUTES = List.of(
            new ServiceRoute("Order", "/api/order"),
            new ServiceRoute("Stock", "/api/stock"),
            new ServiceRoute("Product", "/api/product"),
            new ServiceRoute("Pay", "/api/pay"),
            new ServiceRoute("User", "/api/user")
    );

    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();

    /**
     * Records one request path.
     *
     * @param path request path
     */
    public void record(String path) {
        increment(GATEWAY);
        SERVICE_ROUTES.stream()
                .filter(route -> path != null && path.startsWith(route.pathPrefix()))
                .findFirst()
                .ifPresent(route -> increment(route.name()));
    }

    /**
     * Returns current request counts.
     *
     * @return traffic snapshot
     */
    public GatewayTrafficResponse snapshot() {
        List<GatewayTrafficResponse.ServiceCallItem> items = serviceLabels().stream()
                .map(name -> new GatewayTrafficResponse.ServiceCallItem(name, valueOf(name)))
                .toList();
        return new GatewayTrafficResponse(items);
    }

    private void increment(String serviceName) {
        counters.computeIfAbsent(serviceName, key -> new LongAdder()).increment();
    }

    private long valueOf(String serviceName) {
        LongAdder adder = counters.get(serviceName);
        return adder == null ? 0L : adder.sum();
    }

    private List<String> serviceLabels() {
        return List.of(GATEWAY, "Order", "Stock", "Product", "Pay", "User");
    }

    private record ServiceRoute(String name, String pathPrefix) {
    }
}
