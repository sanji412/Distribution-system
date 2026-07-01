package com.buu.gateway.dto;

import java.util.List;

/**
 * Gateway request traffic snapshot.
 *
 * @param serviceCalls current in-memory request counts grouped by routed service
 */
public record GatewayTrafficResponse(List<ServiceCallItem> serviceCalls) {

    /**
     * Service call chart item.
     *
     * @param name frontend service label
     * @param value request count since Gateway started
     */
    public record ServiceCallItem(String name, long value) {
    }
}
