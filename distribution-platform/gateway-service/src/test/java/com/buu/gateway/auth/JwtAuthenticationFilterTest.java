package com.buu.gateway.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private final GatewayJwtTokenService tokenService = new GatewayJwtTokenService(
            "unit-test-secret-for-jwt",
            "distribution-platform"
    );
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService);

    @Test
    void loginApiCanPassWithoutToken() {
        AtomicBoolean nextCalled = new AtomicBoolean(false);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login").build()
        );

        filter.filter(exchange, markNextCalled(nextCalled)).block();

        assertThat(nextCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void protectedApiRejectsMissingToken() {
        AtomicBoolean nextCalled = new AtomicBoolean(false);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/order/list").build()
        );

        filter.filter(exchange, markNextCalled(nextCalled)).block();

        assertThat(nextCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedApiForwardsUserHeadersWhenTokenIsValid() {
        AtomicBoolean nextCalled = new AtomicBoolean(false);
        String token = tokenService.generateToken(new GatewayUserContext(1L, "admin", "admin"), 120);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/order/list")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, checkedExchange -> {
            assertThat(checkedExchange.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("1");
            assertThat(checkedExchange.getRequest().getHeaders().getFirst("X-Username")).isEqualTo("admin");
            assertThat(checkedExchange.getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("admin");
            nextCalled.set(true);
            return Mono.empty();
        }).block();

        assertThat(nextCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private WebFilterChain markNextCalled(AtomicBoolean nextCalled) {
        return exchange -> {
            nextCalled.set(true);
            return Mono.empty();
        };
    }
}
