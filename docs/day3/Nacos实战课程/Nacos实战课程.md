## 一、版本对齐

### 1.1 严格版本矩阵（必须统一，否则必出兼容问题）

|         组件         |    版本    |                         说明                         |
| :------------------: | :--------: | :--------------------------------------------------: |
|         JDK          |     21     | 全程使用 JDK21 运行，提前确认`java -version`版本正确 |
|     Spring Boot      |   3.2.12   |              学生当前使用版本，全程对齐              |
|     Spring Cloud     |  2023.0.3  |         官方适配 Spring Boot 3.2.x 的稳定版          |
| Spring Cloud Alibaba | 2023.0.1.2 |       官方适配 Spring Boot 3.2.x 的最终稳定版        |
|     Nacos Server     |   2.4.2    |         原生支持 JDK21 运行，稳定无兼容问题          |

### 1.2 核心概念

- **注册中心**：微服务的「通讯录」，服务启动自动登记地址，调用方通过服务名直接查找，不用硬编码 IP
- **配置中心**：微服务的「公告栏」，所有配置统一存放管理，修改后服务自动生效，不用重启
- 两个核心标识：`服务名`（服务的唯一名称）、`Data ID`（配置文件的唯一标识）

------

## 二、Nacos 安装与启动

### 2.1 下载与解压

1. 官网下载二进制包：

   https://github.com/alibaba/nacos/releases/tag/2.4.2选择 `nacos-server-2.4.2.zip`下载

2. 解压到**纯英文、无空格**的目录，例如 `D:\develop\nacos-server-2.4.2`

3. 目录核心结构：

   - `bin`：启动 / 停止脚本
   - `conf`：配置文件
   - `logs`：运行日志

### 2.2 单机模式启动（重点）

> Nacos 默认是集群模式，直接启动会报错，必须加单机参数

1. 进入 `nacos/bin` 目录，在地址栏输入`cmd`回车打开命令窗口
2. 执行启动命令：

```cmd
startup.cmd -m standalone
```

1. 启动成功标志：控制台打印 `Nacos started successfully in stand alone mode`

### 2.3 控制台验证

- 浏览器访问：[http://localhost:8848/nacos](https://link.wtturl.cn/?target=http%3A%2F%2Flocalhost%3A8848%2Fnacos&scene=im&aid=497858&lang=zh)
- 登录成功看到控制台首页，即为安装完成，默认不需要账号密码鉴权，如果需要，账号密码如下：
- 默认账号：`nacos`
- 默认密码：`nacos`

### 2.4 2 个高频启动坑（提前避坑）

1. 启动闪退：检查 JAVA_HOME 是否指向 JDK21，是否加了`-m standalone`参数
2. 端口被占：修改`conf/application.properties`中的`server.port`更换端口

------

## 三、核心实战 1：服务注册与发现

### 3.1 案例场景

实现两个微服务，完成跨服务调用：

- **商品服务（提供者）**：端口 8001，提供商品查询接口
- **订单服务（消费者）**：端口 9001，通过服务名调用商品服务，无需硬编码地址

### 3.2 搭建服务提供者 nacos-goods-provider

#### 步骤 1：创建 Spring Boot 项目

IDEA 新建 Spring Boot 项目，选择 Spring Web 依赖，JDK 选 21。

#### 步骤 2：pom.xml 完整依赖（带注释）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Spring Boot 父工程，统一版本管理 -->
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
        <!-- Web依赖：提供接口能力 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Nacos服务发现核心依赖：自动完成服务注册 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
    </dependencies>

    <!-- 版本统一管理：避免版本不兼容 -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Cloud 版本锁定 -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud Alibaba 版本锁定 -->
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

#### 步骤 3：application.yml 配置

yaml









```properties
server.port=8001

# server name
spring.application.name=nacos-goods-provider

# Nacos
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```

#### 步骤 4：启动类

> Spring Boot 3.x + 新版 Nacos 依赖自动开启服务注册，`@EnableDiscoveryClient`可省略



```java
package com.example.goods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GoodsProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsProviderApplication.class, args);
    }
}
```

#### 步骤 5：业务接口 GoodsController



```java
package com.example.goods.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    // 注入端口，后续验证负载均衡用
    @Value("${server.port}")
    private String serverPort;

    /**
     * 根据ID查询商品接口
     */
    @GetMapping("/{goodsId}")
    public Map<String, Object> getGoodsById(@PathVariable Long goodsId) {
        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goodsId);
        result.put("goodsName", "华为Mate手机");
        result.put("price", 4999);
        result.put("serverPort", serverPort);
        return result;
    }
}
```

#### 步骤 6：注册验证

启动服务，打开 Nacos 控制台 → 服务管理 → 服务列表，能看到`nacos-goods-provider`服务，实例数 1，即为注册成功。

### 3.3 搭建服务消费者 nacos-order-consumer

#### 步骤 1：创建项目，pom 依赖

~~~xml
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
        <spring-cloud.version>2023.0.4</spring-cloud.version>
        <spring-cloud-alibaba.version>2023.0.1.2</spring-cloud-alibaba.version>
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

        <!-- 3. Spring Cloud 负载均衡：@LoadBalanced 注解的核心实现
             必须显式引入，仅在dependencyManagement声明不会实际导入 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
    </dependencies>

    <!-- 依赖版本统一管理：仅锁定版本，不实际引入依赖 -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Cloud 官方BOM：统一Spring Cloud全家桶版本 -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud Alibaba BOM：统一阿里组件版本 -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 构建配置 -->
    <build>
        <plugins>
            <!-- Spring Boot 打包插件 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
~~~



#### 步骤 2：application.yml 配置

```properties
spring.application.name=nacos-order-consumer
server.port=9001
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
```

#### 步骤 3：RestTemplate 配置类（服务调用工具）

> `@LoadBalanced` 开启客户端负载均衡，通过服务名自动选择实例调用

```java
package com.example.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

#### 步骤 4：订单调用接口 OrderController

> 注意：Spring Boot 3.x 已全面迁移至 jakarta 包，`@Resource`从`jakarta.annotation`导入

```java
package com.example.order.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    // 直接用服务名调用，不用写死IP端口
    private static final String GOODS_SERVICE = "http://nacos-goods-provider";

    /**
     * 创建订单：远程调用商品服务查询商品信息
     */
    @GetMapping("/create/{goodsId}")
    public String createOrder(@PathVariable Long goodsId) {
        String url = GOODS_SERVICE + "/goods/" + goodsId;
        Map result = restTemplate.getForObject(url, Map.class);
        return "下单成功，商品信息：" + result;
    }
}
```

### 3.4 联调测试

1. 启动 Nacos、商品服务、订单服务
2. 浏览器访问：[http://localhost:9001/order/create/1](https://link.wtturl.cn/?target=http%3A%2F%2Flocalhost%3A9001%2Forder%2Fcreate%2F1&scene=im&aid=497858&lang=zh)
3. 正常返回商品信息，说明服务调用成功

------

## 四、核心实战 2：配置中心

### 4.1 Nacos 控制台创建配置

1. 控制台 → 配置管理 → 配置列表 → 点击右上角「+」

2. 填写配置信息：

   - Data ID：

     ```
     nacos-goods-provider-dev.yml
     ```

     - 命名规则：`服务名-环境标识.配置格式`，必须严格对应

   - **配置格式**：YAML

   - 配置内容：

     ```yaml
     # 自定义业务配置
     goods:
       stock: 100
       desc: Nacos远程配置-商品库存
     ```

3. 点击「发布」完成配置创建

### 4.2 商品生产服务接入配置中心

#### 步骤 1：新增 pom 依赖

```xml
<!-- Nacos配置中心核心依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

#### 步骤 2：打开application.properties 配置文件

加入下列配置

```yaml
# 原有基础配置（服务端口、服务名、Nacos注册地址）
server.port=8001
spring.application.name=nacos-goods-provider
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

# 新增：Spring Boot 3.x 专属 - 显式声明Nacos配置源（核心变更）
spring.config.import=nacos:nacos-goods-provider-dev.yml?server-addr=127.0.0.1:8848

# 环境标识（可选，若配置Data ID包含环境，需保持一致）
spring.profiles.active=dev
```

#### 步骤 3：代码读取配置 + 动态刷新

修改`GoodsController`，添加`@RefreshScope`注解实现配置动态刷新

```java
package com.example.goods.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @RefreshScope 核心注解：Nacos配置修改后，自动刷新当前类的配置值
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

    @GetMapping("/{goodsId}")
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
     * 测试配置读取接口
     */
    @GetMapping("/config")
    public String testConfig() {
        return "库存：" + goodsStock + "，描述：" + goodsDesc;
    }
}
```

### 4.3 动态刷新测试

1. 重启商品服务，访问 [http://localhost:8001/goods/config](https://link.wtturl.cn/?target=http%3A%2F%2Flocalhost%3A8001%2Fgoods%2Fconfig&scene=im&aid=497858&lang=zh)，能读到远程配置
2. 回到 Nacos 控制台，修改配置，将库存改为 200，点击发布
3. 不重启服务，再次刷新接口，库存自动更新，动态刷新生效

------

## 五、课程总结与排坑

### 5.1 核心步骤回顾

1. Nacos 单机启动必须加`-m standalone`
2. 服务注册：引 discovery 依赖 + 配 nacos 地址，自动注册
3. 服务调用：`@LoadBalanced` + 服务名调用，自动负载均衡
4. 配置中心：引 config+bootstrap 依赖 + bootstrap.yml 配置 + `@RefreshScope`动态刷新

### 5.2 3 个高频报错排查

1. **服务注册不上**：检查 nacos 地址是否正确、服务名无特殊字符、9848 端口未被防火墙拦截
2. **读不到远程配置**：检查 Data ID 命名是否完全匹配、是否引入了 bootstrap 依赖、配置格式缩进是否正确
3. **版本兼容报错**：严格对照本节课的版本矩阵，不要随意升降级