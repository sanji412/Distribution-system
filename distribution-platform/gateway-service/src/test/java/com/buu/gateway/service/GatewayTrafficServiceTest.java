package com.buu.gateway.service;

import com.buu.gateway.dto.GatewayTrafficResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayTrafficServiceTest {

    @Test
    void recordsRequestsByGatewayPathAndReturnsServiceCallItems() {
        GatewayTrafficService service = new GatewayTrafficService();

        service.record("/api/order/dashboard");
        service.record("/api/order/list");
        service.record("/api/stock/list");
        service.record("/api/product/list");
        service.record("/api/unknown/ping");

        GatewayTrafficResponse response = service.snapshot();

        assertThat(response.serviceCalls()).extracting(GatewayTrafficResponse.ServiceCallItem::name)
                .containsExactly("Gateway", "Order", "Stock", "Product", "Pay", "User");
        assertThat(response.serviceCalls()).filteredOn(item -> item.name().equals("Order"))
                .singleElement()
                .extracting(GatewayTrafficResponse.ServiceCallItem::value)
                .isEqualTo(2L);
        assertThat(response.serviceCalls()).filteredOn(item -> item.name().equals("Stock"))
                .singleElement()
                .extracting(GatewayTrafficResponse.ServiceCallItem::value)
                .isEqualTo(1L);
        assertThat(response.serviceCalls()).filteredOn(item -> item.name().equals("Gateway"))
                .singleElement()
                .extracting(GatewayTrafficResponse.ServiceCallItem::value)
                .isEqualTo(5L);
    }
}
