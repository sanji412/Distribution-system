package com.buu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 统一网关启动类
 * 作为前端和外部请求的唯一入口，通过 Nacos 服务名转发到各业务微服务。
 */
@SpringBootApplication
public class GatewayServiceApplication {

    /**
     * 启动统一网关服务。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
