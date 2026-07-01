package com.buu.gateway.dto;

import java.util.List;

/**
 * Service governance overview used by the Vue monitoring page.
 *
 * @param status gateway governance data status
 * @param metrics top metric cards
 * @param logs registry snapshot logs generated from current Nacos discovery data
 * @param instances registered microservice instance status
 * @param routes configured Gateway route definitions
 */
public record GovernanceOverviewResponse(
        String status,
        List<MetricItem> metrics,
        List<String> logs,
        List<InstanceItem> instances,
        List<String> routes
) {

    /**
     * Top metric card item.
     *
     * @param title card title
     * @param value card display value
     * @param tone frontend visual tone
     * @param hint card hint text
     */
    public record MetricItem(String title, String value, String tone, String hint) {
    }

    /**
     * Service instance row item.
     *
     * @param name Nacos service name
     * @param desc business description with ports and instance count
     * @param latency measured TCP latency text
     * @param healthy whether every registered instance responded to TCP probing
     */
    public record InstanceItem(String name, String desc, String latency, boolean healthy) {
    }
}
