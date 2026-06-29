# Nacos+OpenFeign+Gateway 网关实战：统一入口与服务路由

## 一、课前回顾

### 1.1 已完成实战回顾

- 商品服务（nacos-goods-provider:8001）：注册到 Nacos，提供`/goods/{goodsId}`接口，接入 Nacos 配置中心；
- 订单服务（nacos-order-consumer:9001）：注册到 Nacos，通过 OpenFeign 调用商品服务，替代 RestTemplate；
- 核心痛点：各服务独立暴露端口（8001/9001），无统一访问入口，路由管理混乱，无法统一鉴权 / 限流。

### 1.2 Gateway 解决的核心问题

- 统一入口：所有请求通过网关（9501）转发，无需记忆多个端口；
- 服务路由：基于服务名转发请求，适配 Nacos 服务发现；
- 兼容 OpenFeign：网关转发后，订单服务仍可通过 OpenFeign 正常调用商品服务；
- 动态配置：结合 Nacos 配置中心，无需重启网关即可调整路由规则。

## 二、核心理论：Gateway 认知

### 2.1 Gateway 核心概念

|       概念        |                             说明                             |
| :---------------: | :----------------------------------------------------------: |
|   路由（Route）   | 网关核心，由「ID + 断言 + 过滤器 + 目标 URI」组成，匹配断言则转发到目标服务 |
| 断言（Predicate） | 路由匹配规则（如路径、请求头、端口），Spring Cloud Gateway 内置多种断言 |
| 过滤器（Filter）  | 请求 / 响应拦截处理（如添加请求头、限流、日志），分全局 / 局部过滤器 |

### 2.2 Gateway 与 Nacos/OpenFeign 适配关系

```
用户请求 → Gateway（9501）→ Nacos获取服务列表 → 路由转发到订单/商品服务
订单服务内部 → OpenFeign → 从Nacos获取商品服务地址 → 调用商品接口
```

核心：Gateway 和 OpenFeign 均依赖 Nacos 服务发现，底层共用 Spring Cloud LoadBalancer 实现负载均衡，天然兼容。

### 2.3 版本对齐（必须严格遵循）

|         组件         |               版本                |
| :------------------: | :-------------------------------: |
|         JDK          |                21                 |
|     Spring Boot      |              3.2.12               |
|     Spring Cloud     |             2023.0.3              |
| Spring Cloud Alibaba |            2023.0.1.2             |
|     Nacos Server     |               2.4.2               |
| Spring Cloud Gateway | 随 Spring Cloud 2023.0.3 统一管理 |

## 三、环境准备

1. 确保 Nacos Server 2.4.2 单机启动（`startup.cmd -m standalone`）；
2. 启动商品服务（nacos-goods-provider:8001）、订单服务（nacos-order-consumer:9001），确认 Nacos 服务列表可见；
3. IDEA 新建 Spring Boot 项目：`nacos-gateway`（端口 9501），JDK 选 21。

## 四、实战 1：搭建 Gateway 网关工程

### 4.1 pom.xml 完整依赖（带注释）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.12</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>nacos-gateway</artifactId>
    <version>1.0.0</version>
    <name>nacos-gateway</name>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <spring-cloud.alibaba.version>2023.0.1.2</spring-cloud.alibaba.version>
    </properties>

    <dependencies>
        <!-- Gateway核心依赖（内置WebFlux，无需引入Spring Web） -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <!-- Nacos服务发现：Gateway从Nacos获取服务列表 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <!-- 负载均衡：Gateway转发时实现服务实例负载均衡 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <!-- Nacos 配置中心核心依赖：提供 nacos: 配置源解析能力 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud.alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

> 关键注意：Gateway 依赖 WebFlux，**禁止引入 spring-boot-starter-web**，否则启动报错！

### 4.2 本地配置（application.properties）

~~~properties
# 网关服务基础配置
server.port=9501
spring.application.name=nacos-gateway
# Nacos服务发现地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# Gateway路由规则（基于服务名转发）
# 路由1：转发到商品服务
spring.cloud.gateway.routes[0].id=goods-service-route
spring.cloud.gateway.routes[0].uri=lb://nacos-goods-provider # lb=负载均衡，后跟Nacos服务名
spring.cloud.gateway.routes[0].predicates[0]=Path=/goods/** # 匹配路径以/goods开头的请求

# 路由2：转发到订单服务
spring.cloud.gateway.routes[1].id=order-service-route
spring.cloud.gateway.routes[1].uri=lb://nacos-order-consumer
spring.cloud.gateway.routes[1].predicates[0]=Path=/order/**
~~~



```properties
# Gateway service basic settings
server.port=9501
spring.application.name=nacos-gateway

# Nacos service discovery server address
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# Gateway routing rules (forward by service name)
# Route 1: forward requests to goods service
spring.cloud.gateway.routes[0].id=goods-service-route
# lb = load balance, followed by service name registered in Nacos
spring.cloud.gateway.routes[0].uri=lb://nacos-goods-provider
# Match all requests with path starting with /goods
spring.cloud.gateway.routes[0].predicates[0]=Path=/goods/**

# Route 2: forward requests to order service
spring.cloud.gateway.routes[1].id=order-service-route
spring.cloud.gateway.routes[1].uri=lb://nacos-order-consumer
spring.cloud.gateway.routes[1].predicates[0]=Path=/order/**
```

### 4.3 启动类

```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NacosGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosGatewayApplication.class, args);
    }
}
```

### 4.4 启动验证

启动网关服务，确认 Nacos 服务列表新增`nacos-gateway`实例。

## 五、实战 2：Gateway 整合 Nacos 服务发现

### 5.1 测试网关转发到商品服务

启动provider服务，访问网关地址：`http://localhost:9501/goods/1`

预期结果：返回商品信息（和直接访问`8001`端口一致），说明网关成功从 Nacos 获取商品服务地址并转发。

### 5.2 测试网关转发到订单服务

启动consumer服务，访问网关地址：`http://localhost:9501/order/create/1`

预期结果：返回订单调用商品服务的结果（但此时订单服务内部是 OpenFeign 调用，需验证兼容）。

### 5.3 负载均衡验证（可选）

1. 复制商品服务配置，修改端口为 8002，启动第二个商品服务实例；
2. 多次访问`http://localhost:9501/goods/1`，观察返回的`serverPort`在 8001/8002 之间切换，验证 Gateway 负载均衡生效。

## 六、实战 3：Gateway+OpenFeign 兼容调试验证

### 6.1 链路说明

```
用户 → Gateway(9501)/order/create/1 → 订单服务(9001) → OpenFeign → 商品服务(8001)
```

核心：网关仅负责请求转发，订单服务内部的 OpenFeign 调用逻辑完全不受影响，因为 OpenFeign 同样从 Nacos 获取服务地址。

### 6.2 关键兼容点确认

1. 订单服务的 OpenFeign 客户端无需修改（`@FeignClient(name = "nacos-goods-provider")`）；
2. 网关的负载均衡和 OpenFeign 的负载均衡互不冲突（均基于 Spring Cloud LoadBalancer）；

### 6.3 完整链路测试

1. 确保网关、商品、订单服务均启动；
2. 访问网关地址：`http://localhost:9501/order/create/1`；
3. 预期返回：`【OpenFeign调用】下单成功，商品信息：{goodsId=1, goodsName=华为Mate手机, ...}`；
4. 验证日志：订单服务控制台打印 OpenFeign 调用日志，说明网关转发后 OpenFeign 正常工作。

## 七、实战 4：配置中心动态调整 Gateway 路由

### 7.1 Nacos 配置中心创建 yaml 配置

1. Nacos 控制台 → 配置管理 → 新增配置：

   - Data ID：`nacos-gateway-dev.yml`（规则：服务名 - 环境。格式）；

   - 配置格式：YAML；

   - 配置内容：

     ```yaml
     # Nacos配置中心：动态新增路由
     spring:
       cloud:
         gateway:
           routes:
             - id: order-test-route
               uri: lb://nacos-order-consumer
               predicates:
                 - Path=/api/order/**
               filters:
                 - StripPrefix=1  # 去掉第1段路径 /api，转发到后端变为 /order/**
     ```

2. 点击「发布」。

3. 打开nacos-order-consumer服务，在OrderController中加入test接口代码

   ~~~java
   @GetMapping("/test")
       public String testRoute() {
           return "✅ 动态路由生效，这里是订单服务的测试接口，端口9001";
       }
   ~~~

   

### 7.2 网关接入 Nacos 配置中心

修改网关的`application.properties`，新增：

```properties
# 引入Nacos配置中心（Spring Boot 3.x专属）
spring.config.import=nacos:nacos-gateway-dev.yml?server-addr=127.0.0.1:8848
spring.profiles.active=dev
# Dynamic test route: forward to order service
# Strip the 1st path prefix /api before forwarding, final path becomes /order/**
spring.cloud.gateway.routes[2].id=order-test-route
spring.cloud.gateway.routes[2].uri=lb://nacos-order-consumer
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/order/**
spring.cloud.gateway.routes[2].filters[0]=StripPrefix=1
```

### 7.3 动态生效验证

1. 重启网关服务（首次接入配置中心需重启）；
2. 在 Nacos 控制台修改路由规则（如将`/test/**`改为`/order/test/**`），发布配置；
3. 无需重启网关，访问`http://localhost:9501/order/create/1`，验证路由生效。

## 八、排坑与总结

### 8.1 高频坑点

1. Gateway 启动报错：引入了 spring-boot-starter-web，需移除；
2. 路由不生效：检查 Nacos 服务名是否正确、predicates 路径匹配规则；
3. OpenFeign 调用失败：网关转发后请求头丢失（可通过 Gateway 过滤器添加）；
4. 动态配置不生效：Data ID 命名规则错误、未配置`spring.config.import`。

### 8.2 核心总结

1. Gateway 作为统一入口，通过 Nacos 服务发现实现基于服务名的路由转发；
2. Gateway 与 OpenFeign 天然兼容，底层均依赖 Nacos 和 Spring Cloud LoadBalancer；
3. 本地配置用 properties，配置中心用 yaml，兼顾灵活性和规范；
4. 动态路由通过 Nacos 配置中心实现，无需重启网关即可调整规则。