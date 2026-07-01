package com.buu.gateway.service;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Records every request that enters Gateway for the frontend service-call chart.
 */
@Component
public class GatewayTrafficFilter implements GlobalFilter, Ordered {

    private final GatewayTrafficService gatewayTrafficService;

    public GatewayTrafficFilter(GatewayTrafficService gatewayTrafficService) {
        this.gatewayTrafficService = gatewayTrafficService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        gatewayTrafficService.record(exchange.getRequest().getPath().value());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
