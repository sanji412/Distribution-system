package com.buu.gateway.controller;

import com.buu.gateway.common.R;
import com.buu.gateway.dto.GatewayTrafficResponse;
import com.buu.gateway.dto.GovernanceOverviewResponse;
import com.buu.gateway.dto.SystemMonitorResponse;
import com.buu.gateway.service.GatewayTrafficService;
import com.buu.gateway.service.GovernanceOverviewService;
import com.buu.gateway.service.SystemMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service governance controller used by the frontend monitoring page.
 */
@RestController
@RequestMapping("/api/governance")
public class GovernanceController {

    private final GovernanceOverviewService governanceOverviewService;
    private final GatewayTrafficService gatewayTrafficService;
    private final SystemMonitorService systemMonitorService;

    public GovernanceController(
            GovernanceOverviewService governanceOverviewService,
            GatewayTrafficService gatewayTrafficService,
            SystemMonitorService systemMonitorService
    ) {
        this.governanceOverviewService = governanceOverviewService;
        this.gatewayTrafficService = gatewayTrafficService;
        this.systemMonitorService = systemMonitorService;
    }

    /**
     * Queries live Nacos discovery and Gateway route overview data.
     *
     * @return governance overview response
     */
    @GetMapping("/overview")
    public Mono<R<GovernanceOverviewResponse>> overview() {
        return Mono.fromCallable(() -> R.success(governanceOverviewService.getOverview()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Queries request traffic counted by Gateway.
     *
     * @return service call chart data
     */
    @GetMapping("/traffic")
    public Mono<R<GatewayTrafficResponse>> traffic() {
        return Mono.fromCallable(() -> R.success(gatewayTrafficService.snapshot()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Queries runtime and governance data for the system monitor page.
     *
     * @return system monitor response
     */
    @GetMapping("/system-monitor")
    public Mono<R<SystemMonitorResponse>> systemMonitor() {
        return Mono.fromCallable(() -> R.success(systemMonitorService.getMonitor()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
