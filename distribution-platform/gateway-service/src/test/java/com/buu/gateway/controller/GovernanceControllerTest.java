package com.buu.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
@AutoConfigureWebTestClient
class GovernanceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void overviewEndpointReturnsUnifiedResponse() {
        webTestClient.get()
                .uri("/api/governance/overview")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.data.metrics").isArray()
                .jsonPath("$.data.routes").isArray();
    }
}
