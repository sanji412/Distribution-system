package com.buu.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRouteConfigurationTests {

    private final Properties properties = new Properties();

    @BeforeEach
    void loadGatewayProperties() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            assertThat(inputStream).as("gateway application.properties").isNotNull();
            properties.clear();
            properties.load(inputStream);
        }
    }

    @Test
    void gatewayUsesFinalSystemNameAndPort() {
        assertThat(properties.getProperty("spring.application.name")).isEqualTo("gateway-service");
        assertThat(properties.getProperty("server.port")).isEqualTo("9000");
    }

    @Test
    void routesAllBusinessApiPrefixesToNacosServiceNames() {
        assertRoute(0, "user_route", "lb://user-center", "/api/user/**");
        assertRoute(1, "product_route", "lb://product-center", "/api/product/**");
        assertRoute(2, "stock_route", "lb://stock-center", "/api/stock/**");
        assertRoute(3, "order_route", "lb://order-center", "/api/order/**");
        assertRoute(4, "pay_route", "lb://pay-center", "/api/pay/**");
    }

    @Test
    void gatewayAllowsFrontendCrossOriginRequests() {
        assertThat(properties.getProperty("spring.cloud.gateway.globalcors.add-to-simple-url-handler-mapping"))
                .isEqualTo("true");
        assertThat(properties.getProperty("spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origin-patterns"))
                .isEqualTo("*");
        assertThat(properties.getProperty("spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods"))
                .isEqualTo("GET,POST,PUT,DELETE,OPTIONS");
        assertThat(properties.getProperty("spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers"))
                .isEqualTo("*");
        assertThat(properties.getProperty("spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials"))
                .isEqualTo("true");
    }

    private void assertRoute(int index, String routeId, String uri, String pathPattern) {
        String prefix = "spring.cloud.gateway.routes[" + index + "]";
        assertThat(properties.getProperty(prefix + ".id")).isEqualTo(routeId);
        assertThat(properties.getProperty(prefix + ".uri")).isEqualTo(uri);
        assertThat(properties.getProperty(prefix + ".predicates[0]")).isEqualTo("Path=" + pathPattern);
    }
}
