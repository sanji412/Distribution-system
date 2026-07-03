package com.buu.gateway.controller;

import com.buu.gateway.auth.GatewayJwtTokenService;
import com.buu.gateway.auth.GatewayUserContext;
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

    @Autowired
    private GatewayJwtTokenService tokenService;

    @Test
    void overviewEndpointReturnsUnifiedResponse() {
        String token = tokenService.generateToken(new GatewayUserContext(1L, "admin", "admin"), 120);

        webTestClient.get()
                .uri("/api/governance/overview")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.data.metrics").isArray()
                .jsonPath("$.data.routes").isArray();
    }
}
