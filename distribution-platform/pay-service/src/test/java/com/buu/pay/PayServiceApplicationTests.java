package com.buu.pay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "seata.enabled=false"
})
class PayServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
