# Sentinel 服务熔断与限流实战课程方案

## 一、课程目标

1. 理解 Sentinel 核心概念（限流、熔断、降级），能用通俗语言解释其作用
2. 掌握 Sentinel 控制台的安装与启动
3. 基于现有 Nacos 微服务（goods-provider/order-consumer）整合 Sentinel
4. 实现接口限流、远程调用熔断降级功能
5. 排查整合过程中的常见问题

## 二、版本对齐（必须严格遵守，避免兼容问题）

|         组件         |    版本    |                       说明                       |
| :------------------: | :--------: | :----------------------------------------------: |
|         JDK          |     21     |  与 Nacos 课程保持一致，提前验证`java -version`  |
|     Spring Boot      |   3.2.12   |               与现有微服务版本对齐               |
|     Spring Cloud     |  2023.0.3  |              适配 Spring Boot 3.2.x              |
| Spring Cloud Alibaba | 2023.0.1.2 |       适配上述版本，内置 Sentinel 适配逻辑       |
|   Sentinel Console   |   1.8.7    | 支持 JDK21，兼容 Spring Cloud Alibaba 2023.0.1.2 |
|     Nacos Server     |   2.4.2    |                  与现有环境一致                  |

## 三、Sentinel 核心概念

|       概念       |        官方定义        |                           通俗解释                           |
| :--------------: | :--------------------: | :----------------------------------------------------------: |
| 流量控制（限流） | 限制接口 QPS / 并发数  | 像景区限流：每秒只允许 2 个人进，超过的人排队 / 拒绝，防止系统被请求冲垮 |
|     熔断降级     | 服务故障时临时断开调用 | 像家里的保险丝：商品服务挂了，订单服务暂时不调用它，避免一直报错拖垮自己 |
|       资源       |  受保护的接口 / 方法   | 我们要保护的 “目标”，比如`/goods/{goodsId}`接口、远程调用商品服务的方法 |
|       规则       | 限流 / 熔断的配置规则  |    给资源定的 “规矩”，比如 “/goods 接口每秒最多 2 个请求”    |

## 四、Sentinel 控制台安装与启动

### 4.1 下载控制台

官网下载地址：[https://github.com/alibaba/Sentinel/releases/tag/1.8.7](https://link.wtturl.cn/?target=https%3A%2F%2Fgithub.com%2Falibaba%2FSentinel%2Freleases%2Ftag%2F1.8.7&scene=im&aid=497858&lang=zh)

选择 `sentinel-dashboard-1.8.7.jar` 下载（约 50MB）

### 4.2 启动控制台（JDK21 适配）

1. 新建文件夹（纯英文无空格），比如 `D:\develop\sentinel-1.8.7`，将 jar 包放入
2. 打开 cmd 窗口，执行启动命令（指定 JDK21，端口 8080，关闭登录鉴权）：

```
java -jar -Djava.net.preferIPv4Stack=true sentinel-dashboard-1.8.7.jar --server.port=8080 --sentinel.dashboard.auth.enabled=false
```

1. 启动成功标志：控制台打印 `Tomcat started on port(s): 8080 (http)`
2. 访问验证：浏览器打开 `http://localhost:8080`，账号和密码都是`sentinel`，能看到 Sentinel 控制台首页即成功

### 4.3 启动坑点

1. 端口被占：修改启动命令中的`--server.port=8081`（任意未被占用端口）
2. JDK 版本错误：确保`java -version`是 21，启动命令用 JDK21 的 java.exe 执行

## 五、核心实战：整合 Sentinel 到微服务

### 5.1 改造商品服务（nacos-goods-provider）

#### 步骤 1：新增 Sentinel 依赖（修改 pom.xml）

在原有依赖基础上，添加 Sentinel 核心依赖（完整 pom.xml 如下）：

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
    <artifactId>nacos-goods-provider</artifactId>
    <version>1.0.0</version>
    <name>nacos-goods-provider</name>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <spring-cloud.alibaba.version>2023.0.1.2</spring-cloud.alibaba.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- Sentinel核心，包含MVC限流能力 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>

        <!-- 合法servlet采集依赖，groupId是csp不是alibaba.cloud -->
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-web-servlet</artifactId>
            <version>1.8.7</version>
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

#### 步骤 2：修改 application.properties

```properties
# 服务运行端口
server.port=8001
# 当前微服务名称，Nacos注册、配置DataID前缀使用
spring.application.name=nacos-goods-provider
# Nacos注册中心地址，单机本地地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# ========= Nacos配置中心必填配置 =========
# 导入Nacos远程配置，dev环境对应DataID：nacos-goods-provider-dev.yml
spring.config.import=nacos:nacos-goods-provider-dev.yml?server-addr=127.0.0.1:8848
# 指定激活环境，和配置文件后缀dev保持一致
spring.profiles.active=dev
# 关闭Nacos配置导入强制校验（备选，不推荐，优先写上面import）
# spring.cloud.nacos.config.import-check.enabled=false

# ========= Sentinel控制台连接配置 =========
# Sentinel可视化控制台地址
spring.cloud.sentinel.transport.dashboard=127.0.0.1:8080
# 客户端与控制台通信端口，避免端口冲突可修改
spring.cloud.sentinel.transport.port=8719
# 项目启动立刻连接控制台，不用等待第一次访问接口
spring.cloud.sentinel.eager=true
# 统一MVC接口上下文，规范Sentinel资源名称
spring.cloud.sentinel.web-context-unify=true
```



```properties
# Application running port
server.port=8001
# Micro service name, used for nacos register & config dataId
spring.application.name=nacos-goods-provider
# Local nacos discovery server address
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# ========= Required Nacos config import (fix start error) =========
# Import remote nacos config file for dev profile
spring.config.import=nacos:nacos-goods-provider-dev.yml?server-addr=127.0.0.1:8848
# Activate dev environment profile
spring.profiles.active=dev
# Disable mandatory import check (backup option)
# spring.cloud.nacos.config.import-check.enabled=false

# ========= Sentinel dashboard connect config =========
# Sentinel dashboard ip & port
spring.cloud.sentinel.transport.dashboard=127.0.0.1:8080
# Client communication port with dashboard
spring.cloud.sentinel.transport.port=8719
# Connect dashboard immediately after application startup
spring.cloud.sentinel.eager=true
# Unify spring mvc web context for sentinel resource
spring.cloud.sentinel.web-context-unify
```

#### 步骤 3：代码改造（添加限流注解 + 自定义限流处理）

修改`GoodsController`，添加`@SentinelResource`实现接口限流，完整代码如下：

```java
package com.example.goods.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @RefreshScope：Nacos配置动态刷新
 * @RestController：标识为Web接口控制器
 */
@RestController
@RequestMapping("/goods")
@RefreshScope
public class GoodsController {

    @Value("${server.port}")
    private String serverPort;

    // 读取远程Nacos中的自定义配置
    @Value("${goods.stock:0}")
    private Integer goodsStock;

    @Value("${goods.desc:默认描述}")
    private String goodsDesc;

    /**
     * 根据ID查询商品接口（添加Sentinel限流）
     * @SentinelResource：标记为Sentinel受保护资源
     * - value：资源名称（自定义，建议和接口名一致）
     * - blockHandler：限流/熔断时的自定义处理方法
     */
    @GetMapping("/{goodsId}")
    @SentinelResource(value = "goodsDetail", blockHandler = "goodsDetailBlockHandler")
    public Map<String, Object> getGoodsById(@PathVariable Long goodsId) {
        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goodsId);
        result.put("goodsName", "华为Mate手机");
        result.put("price", 4999);
        result.put("stock", goodsStock);
        result.put("desc", goodsDesc);
        result.put("serverPort", serverPort);
        return result;
    }

    /**
     * 商品详情接口限流后的处理方法（必须和blockHandler名称一致）
     * 要求：
     * 1. 方法参数和原方法一致，额外加BlockException参数
     * 2. 返回值和原方法一致
     */
    public Map<String, Object> goodsDetailBlockHandler(Long goodsId, BlockException e) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", 500);
        errorResult.put("msg", "请求太火爆啦！请稍后再试（限流保护）");
        errorResult.put("goodsId", goodsId);
        errorResult.put("exception", e.getClass().getSimpleName());
        return errorResult;
    }

    /**
     * 测试配置读取接口
     */
    @GetMapping("/config")
    public String testConfig() {
        return "库存：" + goodsStock + "，描述：" + goodsDesc;
    }
}
```

### 5.2 改造订单服务（nacos-order-consumer）

#### 步骤 1：新增 Sentinel 依赖（修改 pom.xml）

在原有依赖基础上，添加 Sentinel 核心依赖（完整 pom.xml 如下）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父工程：Spring Boot 官方统一版本管理 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.12</version>
        <relativePath/>
    </parent>

    <!-- 当前项目基本信息 -->
    <groupId>com.example</groupId>
    <artifactId>nacos-order-consumer</artifactId>
    <version>1.0.0</version>
    <name>nacos-order-consumer</name>
    <description>订单服务-服务消费者</description>

    <!-- 全局版本属性统一管理 -->
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <!-- 修复闭合标签不匹配问题 -->
        <spring-cloud.alibaba.version>2023.0.1.2</spring-cloud.alibaba.version>
    </properties>

    <!-- 项目实际引入的依赖 -->
    <dependencies>
        <!-- 1. Spring Web：提供Web接口能力 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 2. Nacos 服务发现：实现服务注册与地址发现 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- 3. Spring Cloud 负载均衡 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>

        <!-- ========== Sentinel核心依赖 ========== -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel-webmvc</artifactId>
        </dependency>
    </dependencies>

    <!-- 依赖版本统一管理 -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud.alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 构建配置 -->
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

#### 步骤 2：修改 application.properties

```properties
# 服务名称（Nacos注册用）
spring.application.name=nacos-order-consumer
# 服务端口
server.port=9001
# Nacos注册中心地址
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# ========== Sentinel核心配置 ==========
# Sentinel控制台地址
spring.cloud.sentinel.transport.dashboard=127.0.0.1:8080
# 客户端与控制台通信端口
spring.cloud.sentinel.transport.port=8720
# 关闭懒加载，启动即连接控制台
spring.cloud.sentinel.eager=true
# 开启RestTemplate的Sentinel适配（远程调用熔断）
spring.cloud.sentinel.rest-template.enabled=true
# 开启Sentinel对Spring MVC接口的自动识别
spring.cloud.sentinel.web-context-unify=true
```

```properties
# Service name (for Nacos registration)
spring.application.name=nacos-order-consumer
# Service port
server.port=9001
# Nacos discovery server address
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# ========== Sentinel Core Config ==========
# Sentinel dashboard address
spring.cloud.sentinel.transport.dashboard=127.0.0.1:8080
# Port for communication between client and dashboard
spring.cloud.sentinel.transport.port=8720
# Disable lazy load, connect dashboard on startup
spring.cloud.sentinel.eager=true
# Enable Sentinel adapt RestTemplate (remote call circuit break)
spring.cloud.sentinel.rest-template.enabled=true
# Enable Sentinel auto recognize Spring MVC interfaces
spring.cloud.sentinel.web-context-unify=true
```

#### 步骤 3：代码改造（远程调用熔断降级）

##### 第一步：修改 RestTemplate 配置（添加 Sentinel 支持）

```java
package com.example.nacosorderconsumer.config;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类：远程调用工具
 */
@Configuration
public class RestTemplateConfig {

    /**
     * @LoadBalanced：开启客户端负载均衡（通过服务名调用）
     * Sentinel自动适配该RestTemplate，实现远程调用熔断
     */
    @Bean
    @LoadBalanced
    @SentinelRestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

##### 第二步：修改 OrderController（添加熔断注解 + 降级处理）

```java
package com.example.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单服务控制器：调用商品服务实现下单
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    // 商品服务地址（通过服务名调用）
    private static final String GOODS_SERVICE = "http://nacos-goods-provider";

    /**
     * 创建订单接口（添加Sentinel熔断降级）
     * @SentinelResource：
     * - value：资源名
     * - blockHandler：限流处理方法
     * - fallback：远程调用失败（异常）的降级处理方法
     */
    @GetMapping("/create/{goodsId}")
    @SentinelResource(
            value = "orderCreate",
            blockHandler = "orderCreateBlockHandler",
            fallback = "orderCreateFallback"
    )
    public String createOrder(@PathVariable Long goodsId) {
        // 远程调用商品服务
        String url = GOODS_SERVICE + "/goods/" + goodsId;
        Map result = restTemplate.getForObject(url, Map.class);
        int i = 1/0;
        return "下单成功，商品信息：" + result;
    }

    /**
     * 订单创建接口限流处理方法
     */
    public String orderCreateBlockHandler(Long goodsId, BlockException e) {
        return "下单失败！请求太频繁，请稍后再试（限流保护），商品ID：" + goodsId;
    }

    /**
     * 订单创建接口降级处理方法（远程调用失败时触发）
     * 要求：参数和原方法一致，额外可加Throwable参数
     */
    public String orderCreateFallback(Long goodsId, Throwable e) {
        // 降级逻辑：返回默认提示，不抛异常
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("code", 200);
        fallbackResult.put("msg", "商品服务暂时不可用，已触发降级保护");
        fallbackResult.put("goodsId", goodsId);
        fallbackResult.put("error", e.getClass().getSimpleName());
        return "下单降级处理：" + fallbackResult;
    }
}
```

## 六、Sentinel 规则配置（控制台操作）

### 6.1 启动所有服务

启动顺序：Nacos → Sentinel 控制台 → 商品服务 → 订单服务

（注：Sentinel 控制台默认懒加载，需先访问一次接口才会显示服务，比如访问`http://localhost:8001/goods/1`以及`http://localhost:9001/order/create/1`）

~~~
java -jar -Djava.net.preferIPv4Stack=true sentinel-dashboard-1.8.7.jar --server.port=8080 --sentinel.dashboard.auth.enabled=false
~~~

~~~
startup.cmd -m standalone
~~~

### 6.2 配置商品服务限流规则

1. 打开 Sentinel 控制台 → 点击左侧「簇点链路」→ 选择`nacos-goods-provider`服务
2. 找到`goodsDetail`资源（商品详情接口）→ 点击右侧「流控」
3. 配置限流规则：
   - 阈值类型：QPS（每秒请求数）
   - 单机阈值：2
   - 其他默认 → 点击「新增」

### 6.3 配置订单服务熔断规则

1. 选择`nacos-order-consumer`服务 → 找到`orderCreate`资源 → 点击右侧「熔断」
2. 配置熔断规则：
   - 熔断策略：异常比例
   - 异常比例阈值：0.5（失败请求占比超过 50% 触发熔断）
   - 熔断时长：5（熔断后 5 秒自动恢复）
   - 最小请求数：5（至少 5 次请求才触发熔断）
   - 统计时长：10000（10 秒内统计）→ 点击「新增」

## 七、联调测试

### 7.1 测试商品服务限流

1. 快速刷新`http://localhost:8001/goods/1`（每秒超过 2 次）
2. 预期结果：触发限流，返回`{"code":500,"msg":"请求太火爆啦！请稍后再试（限流保护）"...}`

### 7.2 测试订单服务熔断

1. 停止商品服务（模拟服务故障）
2. 访问`http://localhost:9001/order/create/1`（连续访问 5 次以上）
3. 预期结果：前 5 次返回降级提示，之后触发熔断，5 秒内访问均返回降级提示，5 秒后恢复

### 7.3 恢复测试

1. 重启商品服务
2. 等待 5 秒后访问`http://localhost:9001/order/create/1`，恢复正常下单

## 八、常见问题与排坑

### 8.1 Sentinel 控制台看不到服务

- 原因 1：未访问接口（Sentinel 懒加载）→ 解决方案：先访问一次接口（如`/goods/1`）
- 原因 2：控制台地址配置错误 → 检查`spring.cloud.sentinel.transport.dashboard`是否为`127.0.0.1:8080`
- 原因 3：端口被占 → 修改`spring.cloud.sentinel.transport.port`（如 8720/8721）

### 8.2 @SentinelResource 注解不生效

- 原因 1：未引入`sentinel-web-servlet`依赖 → 检查 pom.xml 是否添加该依赖
- 原因 2：注解参数错误 → 确保`blockHandler`方法参数 / 返回值和原方法一致
- 原因 3：JDK 版本问题 → 必须使用 JDK21，Sentinel 1.8.7 对 JDK21 兼容

### 8.3 远程调用熔断不生效

- 原因：未开启 RestTemplate 适配 → 检查`spring.cloud.sentinel.rest-template.enabled=true`
- 解决方案：确认依赖`spring-cloud-alibaba-sentinel-webmvc`已引入

### 8.4 启动报错：ClassNotFoundException

- 原因：版本不兼容 → 严格对照版本矩阵，不要混用不同版本的 Spring Cloud/Sentinel

## 九、课程总结

1. Sentinel 核心是 “保护资源”，通过`@SentinelResource`标记资源，控制台配置规则
2. 限流：保护接口不被高并发冲垮，核心是控制 QPS / 并发数
3. 熔断：保护调用方，服务故障时降级，避免雪崩效应
4. 整合关键：添加依赖 + 配置控制台地址 + 注解标记资源 + 自定义处理方法
5. 版本对齐是关键，必须和 Spring Boot/Spring Cloud Alibaba 版本匹配