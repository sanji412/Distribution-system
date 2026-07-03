# 远程 Nacos 控制中心内容

## 1. 文档说明

本文档用于说明“企业级智能分销与订单履约平台”在 Nacos 控制中心中的服务注册与配置中心内容。

提交压缩包时，本文件应与项目源码目录位于压缩包根目录，方便检查服务注册名、远程配置 Data ID、分组和配置内容。

## 2. Nacos 基本信息

| 项目 | 内容 |
| --- | --- |
| Nacos 作用 | 服务注册发现、配置中心 |
| 命名空间 | `public` |
| 配置分组 | `DEFAULT_GROUP` |
| 服务分组 | `DEFAULT_GROUP` |
| 集群名称 | `DEFAULT` |
| 服务实例类型 | 临时实例 |
| 本地 Docker 控制台地址 | `http://localhost:8848/nacos` |
| 当前项目 Docker 可改端口 | `.env` 中的 `NACOS_HOST_PORT` |

说明：如果部署到远程服务器，控制台地址应替换为远程服务器地址，例如 `http://服务器IP:8848/nacos`。本项目提交文档只记录控制中心内容，不记录数据库密码、JWT 密钥、DeepSeek API Key 等敏感信息。

## 3. 服务注册列表

启动系统后，Nacos 服务管理中应看到以下服务。服务名与各模块 `spring.application.name` 保持一致，Gateway 和 OpenFeign 都通过这些服务名完成路由转发或跨服务调用。

| 服务名 | 所属模块 | 端口 | 主要职责 | 健康状态 |
| --- | --- | --- | --- | --- |
| `gateway-service` | 网关服务 | `9000` | 统一入口、路由转发、JWT 鉴权、跨域处理 | 健康 |
| `auth-center` | 鉴权服务 | `8007` | 登录认证、JWT 生成与校验 | 健康 |
| `user-center` | 用户服务 | `8001` | 用户信息查询与用户基础数据管理 | 健康 |
| `product-center` | 商品服务 | `8002` | 商品列表、商品详情、商品配置中心演示 | 健康 |
| `stock-center` | 库存服务 | `8003` | 库存查询、库存扣减、Sentinel 限流、Seata RM | 健康 |
| `order-center` | 订单服务 | `8005` | 订单看板、订单列表、OpenFeign 聚合、Seata TM、AI 客服 | 健康 |
| `pay-center` | 支付服务 | `8006` | 支付单查询、支付单创建、Seata RM | 健康 |

## 4. 配置管理列表

本项目各服务均预留了 Nacos 配置中心接入，配置导入规则为：

```properties
spring.config.import=optional:nacos:服务名-dev.properties?server-addr=${NACOS_SERVER_ADDR:127.0.0.1:8848}
```

其中 Gateway 使用 YAML：

```properties
spring.config.import=optional:nacos:gateway-service-dev.yml?server-addr=${NACOS_SERVER_ADDR:127.0.0.1:8848}
```

建议在 Nacos 配置管理中创建以下配置：

| Data ID | Group | 配置格式 | 说明 |
| --- | --- | --- | --- |
| `gateway-service-dev.yml` | `DEFAULT_GROUP` | YAML | 网关路由与跨域配置 |
| `product-center-dev.properties` | `DEFAULT_GROUP` | Properties | 商品服务配置中心演示 |
| `user-center-dev.properties` | `DEFAULT_GROUP` | Properties | 用户服务预留配置 |
| `stock-center-dev.properties` | `DEFAULT_GROUP` | Properties | 库存服务预留配置 |
| `order-center-dev.properties` | `DEFAULT_GROUP` | Properties | 订单服务预留配置 |
| `pay-center-dev.properties` | `DEFAULT_GROUP` | Properties | 支付服务预留配置 |
| `auth-center-dev.yml` | `DEFAULT_GROUP` | YAML | 鉴权服务预留配置 |

## 5. gateway-service-dev.yml

Data ID：`gateway-service-dev.yml`

Group：`DEFAULT_GROUP`

配置格式：`YAML`

配置内容：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user_route
          uri: lb://user-center
          predicates:
            - Path=/api/user/**
        - id: product_route
          uri: lb://product-center
          predicates:
            - Path=/api/product/**
        - id: stock_route
          uri: lb://stock-center
          predicates:
            - Path=/api/stock/**
        - id: order_route
          uri: lb://order-center
          predicates:
            - Path=/api/order/**
        - id: pay_route
          uri: lb://pay-center
          predicates:
            - Path=/api/pay/**
        - id: auth_route
          uri: lb://auth-center
          predicates:
            - Path=/api/auth/**
      globalcors:
        add-to-simple-url-handler-mapping: true
        cors-configurations:
          '[/**]':
            allowed-origin-patterns:
              - '*'
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers:
              - '*'
            allow-credentials: true
```

说明：

- `lb://服务名` 表示 Gateway 从 Nacos 注册中心获取服务实例，并通过服务名转发。
- `/api/auth/**` 用于登录认证，其余业务接口由 Gateway 统一鉴权后转发。
- JWT 密钥不建议放入提交文档，实际部署时通过环境变量 `JWT_SECRET` 注入。

## 6. product-center-dev.properties

Data ID：`product-center-dev.properties`

Group：`DEFAULT_GROUP`

配置格式：`Properties`

配置内容：

```properties
product.config.stock=100
product.config.desc=Nacos远程配置-商品库存
```

验证接口：

```text
GET /api/product/config
```

预期返回中可以看到：

```json
{
  "serviceName": "product-center",
  "serverPort": "8002",
  "stock": 100,
  "desc": "Nacos远程配置-商品库存"
}
```

说明：该配置用于演示 Nacos 配置中心动态读取能力，`product-service` 中对应 Controller 使用了 `@RefreshScope` 和 `@Value` 读取远程配置。

## 7. user-center-dev.properties

Data ID：`user-center-dev.properties`

Group：`DEFAULT_GROUP`

配置格式：`Properties`

配置内容：

```properties
service.owner=user-center
service.desc=用户中心远程配置
```

说明：用户服务当前主要使用本地配置完成数据库、端口和注册中心连接。该远程配置用于说明服务已具备配置中心接入能力。

## 8. stock-center-dev.properties

Data ID：`stock-center-dev.properties`

Group：`DEFAULT_GROUP`

配置格式：`Properties`

配置内容：

```properties
service.owner=stock-center
service.desc=库存中心远程配置
sentinel.resource.stock-deduct=stock-deduct
```

说明：库存服务已接入 Nacos 注册发现、Sentinel 限流和 Seata AT 分支事务。实际运行时，库存服务通过 `stock-center` 服务名被订单服务 OpenFeign 调用。

## 9. order-center-dev.properties

Data ID：`order-center-dev.properties`

Group：`DEFAULT_GROUP`

配置格式：`Properties`

配置内容：

```properties
service.owner=order-center
service.desc=订单中心远程配置
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=10000
```

说明：订单服务是系统的业务聚合服务，通过 OpenFeign 按 Nacos 服务名调用 `product-center`、`stock-center`、`pay-center`，并作为 Seata 全局事务入口。

## 10. pay-center-dev.properties

Data ID：`pay-center-dev.properties`

Group：`DEFAULT_GROUP`

配置格式：`Properties`

配置内容：

```properties
service.owner=pay-center
service.desc=支付中心远程配置
```

说明：支付服务通过 Nacos 注册为 `pay-center`，订单服务创建订单时通过 OpenFeign 调用支付服务创建支付单，并参与 Seata AT 分布式事务。

## 11. auth-center-dev.yml

Data ID：`auth-center-dev.yml`

Group：`DEFAULT_GROUP`

配置格式：`YAML`

配置内容：

```yaml
auth:
  service-name: auth-center
  desc: 鉴权中心远程配置
jwt:
  issuer: distribution-platform
  expire-minutes: 120
```

说明：鉴权服务负责登录认证和 JWT 签发。`jwt.secret` 属于敏感配置，不写入提交文档，实际部署时通过环境变量提供。

## 12. OpenFeign 与 Nacos 服务发现关系

订单服务中的 OpenFeign 客户端使用 Nacos 服务名进行远程调用：

| 调用方 | 被调用服务名 | 功能 |
| --- | --- | --- |
| `order-center` | `product-center` | 查询商品详情 |
| `order-center` | `stock-center` | 查询库存、扣减库存 |
| `order-center` | `pay-center` | 查询支付单、创建支付单 |

这种方式避免在代码中写死 IP 和端口。服务实例变化后，只要服务重新注册到 Nacos，调用方即可通过服务名发现可用实例。

## 13. Gateway 与 Nacos 服务发现关系

前端请求统一访问 Gateway：

```text
Vue3 前端 -> gateway-service -> Nacos 服务发现 -> 目标微服务
```

Gateway 路由统一使用 `lb://服务名`：

| 前端请求路径 | Gateway 转发目标 |
| --- | --- |
| `/api/auth/**` | `lb://auth-center` |
| `/api/user/**` | `lb://user-center` |
| `/api/product/**` | `lb://product-center` |
| `/api/stock/**` | `lb://stock-center` |
| `/api/order/**` | `lb://order-center` |
| `/api/pay/**` | `lb://pay-center` |

## 14. 验证方式

### 14.1 验证服务注册

进入 Nacos 控制台：

```text
服务管理 -> 服务列表
```

检查是否存在：

```text
gateway-service
auth-center
user-center
product-center
stock-center
order-center
pay-center
```

每个服务实例应显示为健康状态。

### 14.2 验证配置中心

进入 Nacos 控制台：

```text
配置管理 -> 配置列表
```

检查是否存在本文档第 4 节列出的 Data ID。

商品配置可通过接口验证：

```bash
curl http://localhost:8002/api/product/config
```

通过 Gateway 验证时，需要先登录获取 JWT，再访问：

```bash
curl http://localhost:9000/api/product/config \
  -H "Authorization: Bearer 登录后返回的token"
```

### 14.3 验证 Gateway 通过 Nacos 转发

登录接口：

```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

商品列表接口：

```bash
curl http://localhost:9000/api/product/list \
  -H "Authorization: Bearer 登录后返回的token"
```

如果接口返回统一 JSON 结果，说明 Gateway 已经通过 Nacos 服务名转发到对应服务。

## 15. 注意事项

1. 本项目 Docker 一键演示为了保证迁移稳定，核心配置仍保留在各服务 `application.properties` 中。
2. Nacos 配置中心内容用于课程作业展示和远程配置演示，可以按本文档在控制台创建。
3. 数据库密码、JWT 密钥、DeepSeek API Key 等敏感内容不应写入 Nacos 提交文档，应使用环境变量或 `.env` 文件管理。
4. 如果使用 Docker Compose 启动，Nacos 控制台端口可能由 `.env` 中的 `NACOS_HOST_PORT` 修改，访问地址以实际端口为准。
5. 如果服务刚启动后 Nacos 页面暂时没有实例，可等待 30 到 60 秒后刷新服务列表。
