# Seata 分布式事务实战完整课程方案

**前置知识**：Spring Boot 基础、MyBatis 使用、微服务远程调用概念

**教学目标**：理解分布式事务核心价值，快速搭建 Seata 环境，通过电商下单场景跑通 AT 模式分布式事务，掌握核心配置与注解用法

**版本说明**：Spring Boot 3.2.12、Spring Cloud Alibaba 2023.0.1.2、Seata Server 2.0.0、MySQL 8.0

------

## 一、核心理论

### 1.1 分布式事务问题引入

**场景**：电商下单包含 3 个独立微服务操作

- 订单服务：创建订单（订单库）
- 库存服务：扣减商品库存（库存库）
- 账户服务：扣减用户余额（账户库）

**问题本质**：3 个服务对应 3 个独立数据库，本地事务只能保证自身数据一致，某一步失败时，其他服务已提交的数据无法自动撤回，最终导致数据不一致（超卖、资损）。

### 1.2 Seata 三大核心角色

1. **TC（事务协调器）**：即 Seata Server 服务，负责全局事务的统一调度，判定最终提交或回滚，相当于 “全局裁判”。
2. **TM（事务管理器）**：事务发起方（本案例为订单服务），通过 `@GlobalTransactional` 注解标记事务入口，向 TC 注册全局事务。
3. **RM（资源管理器）**：每个业务服务都是 RM，管理自身数据库，配合 TC 执行本地事务的提交 / 回滚。

### 1.3 AT 模式核心原理

Seata 默认的零侵入模式，也是企业最常用方案，基于两阶段提交改进：

- **一阶段**：执行业务 SQL 的同时，自动记录数据修改前后的快照（存入 `undo_log` 表），本地事务直接提交，释放数据库连接。
- **二阶段**：
  - 全部成功：TC 通知所有服务异步删除 `undo_log` 快照
  - 任一失败：TC 通知所有服务根据 `undo_log` 自动反向补偿，恢复数据

------

## 二、环境快速搭建

### 2.1 Seata Server 安装启动

1. **下载安装包**

   官网下载地址：[https://seata.apache.org/zh-cn/blog/download](https://link.wtturl.cn/?target=https%3A%2F%2Fseata.apache.org%2Fzh-cn%2Fblog%2Fdownload&scene=im&aid=497858&lang=zh)

   选择版本：`seata-server-2.0.0.tar.gz`（Windows 下载 zip 包）

2. **解压文件**

   **注意**：必须解压到纯英文、无空格、无中文的路径

   课程中使用 **file 单机存储模式**，无需额外数据库，默认配置即可直接启动。如需修改，配置文件路径：`conf/application.yml`

   核心配置项说明（如需修改可在此调整）：

   ```yaml
   server:
     port: 7091        # Web控制台访问端口

   seata:
     server:
       service-port: 8091  # 业务服务连接的通信端口
     store:
       mode: file          # 存储模式：file(单机教学用)、db(高可用)、redis
   ```

3. **启动服务**

   **方式一**：命令行启动（推荐，可查看启动日志）

   能看到完整启动信息，方便排查问题，教学首选。

   1. 进入 `bin` 目录，在地址栏输入 `cmd` 回车，快速打开当前目录的命令窗口

   2. 执行启动命令：

      ```bash
      seata-server.bat
      ```

   3. 控制台出现 `Seata Server started successfully` 字样，即为启动成功。

   **方式二**：双击启动

   直接双击 `bin` 目录下的 `seata-server.bat` 即可启动，会弹出一个命令行窗口。

   > 注意：窗口关闭则服务停止，请勿关闭启动窗口。

4. **验证启动成功**

   **方式一**：Web 控制台验证

   打开浏览器访问：`http://localhost:7091`，默认账号密码均为：`seata`，能正常登录并看到控制台首页，即为服务启动成功。

   **方式二：**端口验证

   在 cmd 中执行命令，能查到端口监听即为正常：

   ~~~bash
   netstat -ano | findstr :8091
   ~~~

5. **停止服务**

   在启动的命令行窗口中按 `Ctrl + C`，输入 `Y` 回车即可停止

6. **常见问题排查**

   1. 双击 bat 后窗口一闪而过（闪退）

      **Java 环境未配置**

      最常见原因。在 cmd 中执行 `java -version` 验证，若报错则先配置 `JAVA_HOME` 环境变量。

      兜底方案：右键编辑 `seata-server.bat`，找到以下行，手动指定 java.exe 绝对路径：

      ```bat
      if "%JAVACMD%"=="" set JAVACMD=C:\Program Files\Java\jdk1.8.0_301\bin\java.exe
      ```

      **路径含中文 / 空格**

      将 Seata 目录移动到纯英文无空格路径下，例如 `D:\seata-server-2.0.0`。

      **JDK 位数不匹配**

      必须使用 64 位 JDK，32 位 JDK 会因内存不足启动失败。

   2. 8091 端口被占用

      **查找占用进程**：

      ```
      netstat -ano | findstr :8091
      ```

      记录最后一列的进程 PID，执行结束进程：

      ```
      taskkill /PID 进程ID /F
      ```

      或修改 `conf/application.yml` 中的 `service-port` 为其他端口，同步修改业务服务的 Seata 配置地址。

   3.

### 2.2 数据库全量脚本

创建 3 个独立业务库，**每个库都必须执行 `undo_log` 表创建语句**，这是 AT 模式自动回滚的基础。

```sql
-- =============================================
-- 1. 创建订单库并初始化
-- =============================================
CREATE DATABASE IF NOT EXISTS seata_order DEFAULT CHARACTER SET utf8mb4;
USE seata_order;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  product_id BIGINT NOT NULL COMMENT '商品ID',
  count INT NOT NULL COMMENT '购买数量',
  money DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  status INT DEFAULT 0 COMMENT '状态：0-创建中，1-已完成'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 回滚日志表（必建）
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id BIGINT NOT NULL COMMENT '分支事务ID',
  xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  context VARCHAR(128) NOT NULL COMMENT '上下文信息',
  rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
  log_status INT NOT NULL COMMENT '日志状态',
  log_created DATETIME NOT NULL COMMENT '创建时间',
  log_modified DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (branch_id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 2. 创建库存库并初始化
-- =============================================
CREATE DATABASE IF NOT EXISTS seata_storage DEFAULT CHARACTER SET utf8mb4;
USE seata_storage;

-- 库存表
CREATE TABLE IF NOT EXISTS t_storage (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL COMMENT '商品ID',
  total INT NOT NULL COMMENT '总库存',
  used INT DEFAULT 0 COMMENT '已用库存'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化测试数据：商品1总库存100
INSERT INTO t_storage (product_id, total, used) VALUES (1, 100, 0);

-- 回滚日志表（必建）
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id BIGINT NOT NULL COMMENT '分支事务ID',
  xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  context VARCHAR(128) NOT NULL COMMENT '上下文信息',
  rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
  log_status INT NOT NULL COMMENT '日志状态',
  log_created DATETIME NOT NULL COMMENT '创建时间',
  log_modified DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (branch_id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 3. 创建账户库并初始化
-- =============================================
CREATE DATABASE IF NOT EXISTS seata_account DEFAULT CHARACTER SET utf8mb4;
USE seata_account;

-- 账户表
CREATE TABLE IF NOT EXISTS t_account (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  balance DECIMAL(10,2) NOT NULL COMMENT '账户余额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化测试数据：用户1余额1000元
INSERT INTO t_account (user_id, balance) VALUES (1, 1000.00);

-- 回滚日志表（必建）
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id BIGINT NOT NULL COMMENT '分支事务ID',
  xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  context VARCHAR(128) NOT NULL COMMENT '上下文信息',
  rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
  log_status INT NOT NULL COMMENT '日志状态',
  log_created DATETIME NOT NULL COMMENT '创建时间',
  log_modified DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (branch_id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 库存表添加索引
USE seata_storage;
ALTER TABLE t_storage ADD INDEX idx_product_id (product_id);

-- 账户表添加索引（提前优化）
USE seata_account;
ALTER TABLE t_account ADD INDEX idx_user_id (user_id);
```

------

## 三、项目代码完整实现

### 3.1 项目整体架构

共 3 个微服务，均为 Spring Boot 工程：

|     服务名      | 端口 |       角色       |    数据库     |
| :-------------: | :--: | :--------------: | :-----------: |
| storage-service | 8082 |   RM（资源方）   | seata_storage |
| account-service | 8083 |   RM（资源方）   | seata_account |
|  order-service  | 8081 | TM（事务发起方） |  seata_order  |

------

父工程pom.xml

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 父工程基础坐标 -->
    <groupId>com.example</groupId>
    <artifactId>seata-demo-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>seata-demo-parent</name>
    <description>Seata分布式事务实战-父工程（统一版本管理）</description>

    <!-- 多模块父工程必须指定打包方式为pom -->
    <packaging>pom</packaging>

    <!-- 聚合所有子模块，名称与文件夹名完全一致 -->
    <modules>
        <module>order-service</module>
        <module>storage-service</module>
        <module>account-service</module>
    </modules>

    <!-- 继承Spring Boot官方父工程，统一管理基础依赖 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.12</version>
        <relativePath/>
    </parent>

    <!-- 全局版本统一管理，所有子模块共享 -->
    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- Spring Cloud 官方版本：适配Spring Boot 3.2.12 -->
        <spring-cloud.version>2023.0.4</spring-cloud.version>
        <!-- Spring Cloud Alibaba 版本：适配Spring Boot 3.2.x -->
        <spring-cloud-alibaba.version>2023.0.1.2</spring-cloud-alibaba.version>
        <!-- MyBatis-Plus 稳定版 -->
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
    </properties>

    <!-- 所有子模块通用依赖（自动继承，无需重复声明） -->
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <!-- 依赖版本管理：仅声明版本，子模块按需引入，无需写version -->
    <dependencyManagement>
        <dependencies>
            <!-- 1. Spring Cloud 官方全生态BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- 2. Spring Cloud Alibaba 全生态BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- 3. MyBatis-Plus 版本锁定 -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>org.mybatis</groupId>
                <artifactId>mybatis-spring</artifactId>
                <version>3.0.3</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 构建插件统一管理 -->
    <build>
        <pluginManagement>
            <plugins>
                <!-- Spring Boot 打包插件 -->
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
~~~



### 3.2 库存服务 storage-service（完整代码）

#### 3.2.1 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.15</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>storage-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>storage-service</name>

    <properties>
        <java.version>17</java.version>
        <spring-cloud-alibaba.version>2023.0.1.2</spring-cloud-alibaba.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
    </properties>

    <dependencies>
        <!-- Spring Web 基础依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Seata 分布式事务客户端 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        </dependency>

        <!-- MyBatis-Plus ORM框架 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL 8.0 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok 简化代码 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <!-- 统一版本管理 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
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
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 3.2.2 application.properties

```properties
# 服务端口
server.port=8082
# 服务名称
spring.application.name=storage-service

# 数据源配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_storage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus 配置
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata 核心配置 ====================
# 开启Seata功能
seata.enabled=true
# 应用唯一标识，一般使用服务名
seata.application-id=${spring.application.name}
# 事务分组名称，同一业务集群保持一致
seata.tx-service-group=my_tx_group
# 事务分组映射到TC（事务协调器）集群名称
seata.service.vgroup-mapping.my_tx_group=default
# TC（事务协调器）服务地址（Seata Server通信端口）
seata.service.grouplist.default=localhost:8091
# 开启数据源自动代理（AT模式核心）
seata.enable-auto-data-source-proxy=true
# 数据源代理模式：AT
seata.data-source-proxy-mode=AT
# 仅当前服务的数据库连接使用读已提交隔离级别，不影响MySQL全局和其他库
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED
# Seata 锁重试核心配置（解决超时）
# 锁重试间隔（毫秒）
seata.client.rm.lock.retry-interval=50
# 锁重试次数（默认30，增加到50）
seata.client.rm.lock.retry-times=50
# 锁等待超时时间（默认5000，增加到30000）
seata.client.rm.lock.lock-wait-timeout=30000
# 冲突时分支回滚，避免死锁
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
```

~~~properties
# Server port
server.port=8082
# Service application name
spring.application.name=storage-service

# Datasource configuration
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_storage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus configuration
# Enable underscore to camel case mapping
mybatis-plus.configuration.map-underscore-to-camel-case=true
# Print SQL logs to console
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata Core Configuration ====================
# Enable Seata distributed transaction
seata.enabled=true
# Unique application ID, use service name by default
seata.application-id=${spring.application.name}
# Transaction service group, keep the same for all services in one cluster
seata.tx-service-group=my_tx_group
# Map transaction group to TC cluster name
seata.service.vgroup-mapping.my_tx_group=default
# TC (Seata Server) communication address
seata.service.grouplist.default=localhost:8091
# Enable auto datasource proxy (core feature for AT mode)
seata.enable-auto-data-source-proxy=true
# Datasource proxy mode: AT
seata.data-source-proxy-mode=AT
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED

seata.client.rm.lock.retry-interval=50
seata.client.rm.lock.retry-times=50
seata.client.rm.lock.lock-wait-timeout=30000
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
~~~



#### 3.2.3 启动类 StorageServiceApplication.java



```java
package com.example.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存服务启动类
 */
@SpringBootApplication
@MapperScan("com.example.storage.mapper")
public class StorageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }
}
```

#### 3.2.4 实体类 Storage.java



```java
package com.example.storage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库存实体类
 * 对应数据库表 t_storage
 */
@Data
@TableName("t_storage")
public class Storage {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 总库存数量
     */
    private Integer total;

    /**
     * 已使用库存数量
     */
    private Integer used;
}
```

#### 3.2.5 数据访问层 StorageMapper.java



```java
package com.example.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.storage.entity.Storage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存数据访问层
 * 继承BaseMapper获得基础CRUD能力
 */
@Mapper
public interface StorageMapper extends BaseMapper<Storage> {

    /**
     * 扣减库存：增加已用库存，校验库存充足
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE t_storage SET used = used + #{count} WHERE product_id = #{productId} AND total - used >= #{count}")
    int decreaseStorage(@Param("productId") Long productId, @Param("count") Integer count);
}
```

#### 3.2.6 业务层 StorageService.java



```java
package com.example.storage.service;

import com.example.storage.mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 库存业务逻辑层
 * 作为Seata的RM角色，无需添加额外事务注解
 * Seata会自动代理数据源，管理分支事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageMapper storageMapper;

    /**
     * 扣减商品库存
     * @param productId 商品ID
     * @param count 扣减数量
     */
    public void decrease(Long productId, Integer count) {
        log.info("【库存服务】开始扣减库存，商品ID：{}，扣减数量：{}", productId, count);

        int rows = storageMapper.decreaseStorage(productId, count);
        if (rows == 0) {
            throw new RuntimeException("库存不足，扣减失败");
        }

        log.info("【库存服务】库存扣减成功");
    }
}
```

#### 3.2.7 控制层 StorageController.java



```java
package com.example.storage.controller;

import com.example.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存对外接口
 * 提供给订单服务通过Feign远程调用
 */
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * 扣减库存接口
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 操作结果
     */
    @PostMapping("/decrease")
    public String decrease(@RequestParam Long productId, @RequestParam Integer count) {
        storageService.decrease(productId, count);
        return "库存扣减成功";
    }
}
```

------

### 3.3 账户服务 account-service（完整代码）

#### 3.3.1 pom.xml

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承父工程，显式指定父pom相对路径 -->
    <parent>
        <groupId>com.example</groupId>
        <artifactId>seata-demo-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 当前模块信息 -->
    <artifactId>account-service</artifactId>
    <name>account-service</name>
    <description>账户服务-分布式事务资源方（RM）</description>

    <!-- 当前模块专属依赖 -->
    <dependencies>
        <!-- Spring Web 核心 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Seata 分布式事务客户端 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        </dependency>

        <!-- MyBatis-Plus ORM -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>

        <!-- MySQL 8.0 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <!-- 构建插件 -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
~~~



#### 3.3.2 application.properties

```properties
# 服务端口
server.port=8083
# 服务名称
spring.application.name=account-service

# 数据源配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_account?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus 配置
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata 核心配置 ====================
seata.enabled=true
seata.application-id=${spring.application.name}
seata.tx-service-group=my_tx_group
seata.service.vgroup-mapping.my_tx_group=default
seata.service.grouplist.default=localhost:8091
seata.enable-auto-data-source-proxy=true
seata.data-source-proxy-mode=AT
# 仅当前服务的数据库连接使用读已提交隔离级别，不影响MySQL全局和其他库
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED
# Seata 锁重试核心配置（解决超时）
# 锁重试间隔（毫秒）
seata.client.rm.lock.retry-interval=50
# 锁重试次数（默认30，增加到50）
seata.client.rm.lock.retry-times=50
# 锁等待超时时间（默认5000，增加到30000）
seata.client.rm.lock.lock-wait-timeout=30000
# 冲突时分支回滚，避免死锁
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
```

~~~properties
# Server port
server.port=8083
# Service application name
spring.application.name=account-service

# Datasource configuration
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_account?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus configuration
# Enable underscore to camel case mapping
mybatis-plus.configuration.map-underscore-to-camel-case=true
# Print SQL logs to console
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata Core Configuration ====================
# Enable Seata distributed transaction
seata.enabled=true
# Unique application ID, use service name by default
seata.application-id=${spring.application.name}
# Transaction service group, keep the same for all services in one cluster
seata.tx-service-group=my_tx_group
# Map transaction group to TC cluster name
seata.service.vgroup-mapping.my_tx_group=default
# TC (Seata Server) communication address
seata.service.grouplist.default=localhost:8091
# Enable auto datasource proxy (core feature for AT mode)
seata.enable-auto-data-source-proxy=true
# Datasource proxy mode: AT
seata.data-source-proxy-mode=AT
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED

seata.client.rm.lock.retry-interval=50
seata.client.rm.lock.retry-times=50
seata.client.rm.lock.lock-wait-timeout=30000
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
~~~



#### 3.3.3 启动类 AccountServiceApplication.java



```java
package com.example.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 账户服务启动类
 */
@SpringBootApplication
@MapperScan("com.example.account.mapper")
public class AccountServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
```

#### 3.3.4 实体类 Account.java



```java
package com.example.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户实体类
 * 对应数据库表 t_account
 */
@Data
@TableName("t_account")
public class Account {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户余额
     */
    private BigDecimal balance;
}
```

#### 3.3.5 数据访问层 AccountMapper.java



```java
package com.example.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.account.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户数据访问层
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 扣减账户余额，校验余额充足
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 影响行数
     */
    @Update("UPDATE t_account SET balance = balance - #{money} WHERE user_id = #{userId} AND balance >= #{money}")
    int decreaseBalance(@Param("userId") Long userId, @Param("money") BigDecimal money);
}
```

#### 3.3.6 业务层 AccountService.java



```java
package com.example.account.service;

import com.example.account.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 账户业务逻辑层
 * RM角色，Seata自动管理分支事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;

    /**
     * 扣减用户账户余额
     * @param userId 用户ID
     * @param money 扣减金额
     */
    public void decrease(Long userId, BigDecimal money) {
        log.info("【账户服务】开始扣减余额，用户ID：{}，扣减金额：{}", userId, money);

        int rows = accountMapper.decreaseBalance(userId, money);
        if (rows == 0) {
            throw new RuntimeException("账户余额不足，扣减失败");
        }

        log.info("【账户服务】余额扣减成功");
    }
}
```

#### 3.3.7 控制层 AccountController.java



```java
package com.example.account.controller;

import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 账户对外接口
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 扣减余额接口
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 操作结果
     */
    @PostMapping("/decrease")
    public String decrease(@RequestParam Long userId, @RequestParam BigDecimal money) {
        accountService.decrease(userId, money);
        return "账户余额扣减成功";
    }
}
```

------

### 3.4 订单服务 order-service（事务发起方，完整代码）

#### 3.4.1 pom.xml

在库存服务依赖基础上，新增 OpenFeign 远程调用依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承父工程，显式指定父pom相对路径 -->
    <parent>
        <groupId>com.example</groupId>
        <artifactId>seata-demo-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <!-- 当前模块信息 -->
    <artifactId>storage-service</artifactId>
    <name>storage-service</name>
    <description>库存服务-分布式事务资源方（RM）</description>

    <!-- 当前模块专属依赖 -->
    <dependencies>
        <!-- Spring Web 核心 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Seata 分布式事务客户端 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        </dependency>

        <!-- MyBatis-Plus ORM -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>

        <!-- MySQL 8.0 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <!-- 构建插件 -->
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

#### 3.4.2 application.properties

```properties
# 服务端口
server.port=8081
# 服务名称
spring.application.name=order-service

# 数据源配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_order?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus 配置
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata 核心配置 ====================
seata.enabled=true
seata.application-id=${spring.application.name}
seata.tx-service-group=my_tx_group
seata.service.vgroup-mapping.my_tx_group=default
seata.service.grouplist.default=localhost:8091
seata.enable-auto-data-source-proxy=true
seata.data-source-proxy-mode=AT
# 仅当前服务的数据库连接使用读已提交隔离级别，不影响MySQL全局和其他库
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED
# Seata 锁重试核心配置（解决超时）
# 锁重试间隔（毫秒）
seata.client.rm.lock.retry-interval=50
# 锁重试次数（默认30，增加到50）
seata.client.rm.lock.retry-times=50
# 锁等待超时时间（默认5000，增加到30000）
seata.client.rm.lock.lock-wait-timeout=30000
# 冲突时分支回滚，避免死锁
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
```

~~~properties
# Server port
server.port=8081
# Service application name
spring.application.name=order-service

# Datasource configuration
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/seata_order?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=root

# MyBatis-Plus configuration
# Enable underscore to camel case mapping
mybatis-plus.configuration.map-underscore-to-camel-case=true
# Print SQL logs to console
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ==================== Seata Core Configuration ====================
# Enable Seata distributed transaction
seata.enabled=true
# Unique application ID, use service name by default
seata.application-id=${spring.application.name}
# Transaction service group, keep the same for all services in one cluster
seata.tx-service-group=my_tx_group
# Map transaction group to TC cluster name
seata.service.vgroup-mapping.my_tx_group=default
# TC (Seata Server) communication address
seata.service.grouplist.default=localhost:8091
# Enable auto datasource proxy (core feature for AT mode)
seata.enable-auto-data-source-proxy=true
# Datasource proxy mode: AT
seata.data-source-proxy-mode=AT
spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED

seata.client.rm.lock.retry-interval=50
seata.client.rm.lock.retry-times=50
seata.client.rm.lock.lock-wait-timeout=30000
seata.client.rm.lock.retry-policy-branch-rollback-on-conflict=true
~~~



#### 3.4.3 启动类 OrderServiceApplication.java



```java
package com.example.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * @EnableFeignClients 开启Feign远程调用客户端
 */
@SpringBootApplication
@MapperScan("com.example.order.mapper")
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

#### 3.4.4 实体类 Order.java



```java
package com.example.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单实体类
 * 对应数据库表 t_order
 */
@Data
@TableName("t_order")
public class Order {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单总金额
     */
    private BigDecimal money;

    /**
     * 订单状态：0-创建中，1-已完成
     */
    private Integer status;
}
```

#### 3.4.5 数据访问层 OrderMapper.java



```java
package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
```

#### 3.4.6 Feign 远程调用接口

**库存服务调用接口 StorageFeignClient.java**



```java
package com.example.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务远程调用客户端
 * name: 服务名称
 * url: 服务地址（无注册中心时直接指定）
 */
@FeignClient(name = "storage-service", url = "http://localhost:8082")
public interface StorageFeignClient {

    /**
     * 调用库存服务扣减库存
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 操作结果
     */
    @PostMapping("/storage/decrease")
    String decrease(@RequestParam("productId") Long productId,
                    @RequestParam("count") Integer count);
}
```

**账户服务调用接口 AccountFeignClient.java**



```java
package com.example.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 账户服务远程调用客户端
 */
@FeignClient(name = "account-service", url = "http://localhost:8083")
public interface AccountFeignClient {

    /**
     * 调用账户服务扣减余额
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 操作结果
     */
    @PostMapping("/account/decrease")
    String decrease(@RequestParam("userId") Long userId,
                    @RequestParam("money") BigDecimal money);
}
```

#### 3.4.7 业务层 OrderService.java（核心事务入口）



```java
package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.feign.AccountFeignClient;
import com.example.order.feign.StorageFeignClient;
import com.example.order.mapper.OrderMapper;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final StorageFeignClient storageFeignClient;
    private final AccountFeignClient accountFeignClient;

    /**
     * 创建订单（全局分布式事务入口）
     * @GlobalTransactional 管控跨服务的全局分布式事务
     * @Transactional 管控当前服务内的本地数据库事务，将多次DB操作合并为一个分支事务
     */
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("【订单服务】全局事务启动，XID：{}", RootContext.getXID());

        // 1. 本地创建订单（初始状态0）
        log.info("【订单服务】开始创建订单");
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(0);
        orderMapper.insert(order);
        log.info("【订单服务】订单创建成功，订单ID：{}", order.getId());

        // 2. 远程调用：扣减商品库存
        log.info("【订单服务】调用库存服务扣减库存");
        storageFeignClient.decrease(productId, count);

        // 3. 远程调用：扣减用户账户余额
        log.info("【订单服务】调用账户服务扣减余额");
        accountFeignClient.decrease(userId, money);

        // 4. 本地更新订单状态为已完成
        log.info("【订单服务】更新订单状态为已完成");
        order.setStatus(1);
        orderMapper.updateById(order);

        // ========== 测试回滚：放开注释模拟异常 ==========
        int i = 1 / 0;

        log.info("【订单服务】全局事务执行完成，XID：{}", RootContext.getXID());
    }
}
```

#### 3.4.8 控制层 OrderController.java



```java
package com.example.order.controller;

import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 订单对外接口
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单接口
     * @param userId 用户ID
     * @param productId 商品ID
     * @param count 购买数量
     * @param money 订单金额
     * @return 操作结果
     */
    @PostMapping("/create")
    public String createOrder(@RequestParam Long userId,
                              @RequestParam Long productId,
                              @RequestParam Integer count,
                              @RequestParam BigDecimal money) {
        orderService.createOrder(userId, productId, count, money);
        return "订单创建成功，分布式事务执行完成";
    }
}
```

------

## 四、测试验证与课程总结

### 4.1 启动顺序

严格按照以下顺序启动，避免连接失败：

1. 启动 Seata Server
2. 启动 storage-service（8082）
3. 启动 account-service（8083）
4. 启动 order-service（8081）

### 4.2 正常提交测试

**请求接口**：

```
POST http://localhost:8081/order/create
参数：userId=1&productId=1&count=10&money=100
```

**预期结果**：

- 订单库 `t_order` 新增 1 条记录，`status=1`

- 库存库 `t_storage` 中 `used` 从 0 变为 10

- 账户库 `t_account` 中 `balance` 从 1000 变为 900

- 控制台日志可看到全局事务 XID，三个服务均执行成功

  ~~~sql
  SELECT
    (SELECT used FROM seata_storage.t_storage WHERE product_id = 1) AS 已用库存,
    (SELECT balance FROM seata_account.t_account WHERE user_id = 1) AS 账户余额,
    (SELECT id FROM seata_order.t_order WHERE user_id = 1 LIMIT 1) AS 订单ID,
    (SELECT `status` FROM seata_order.t_order WHERE user_id = 1 LIMIT 1) AS 订单状态,
    (SELECT money FROM seata_order.t_order WHERE user_id = 1 LIMIT 1) AS 订单金额;
  ~~~

### 4.3 全局回滚测试

1. 打开 `OrderService.java`，放开模拟异常代码：`int i = 1 / 0;`
2. 重启订单服务，再次调用上述接口

**预期结果**：

- 接口返回异常报错
- 三个数据库数据均无变化（订单无新增、库存不变、余额不变）
- 控制台输出回滚日志，验证分布式事务生效
- 如果测试时出现异常情况，需要重新启动Seata以及重新按顺序启动三个服务，并且在数据库中执行下列语句。全部完成之后再次测试。

~~~sql
-- 清空三个库的回滚日志
TRUNCATE TABLE seata_order.undo_log;
TRUNCATE TABLE seata_storage.undo_log;
TRUNCATE TABLE seata_account.undo_log;

-- 重置订单、库存、账户数据
DELETE FROM seata_order.t_order;
UPDATE seata_storage.t_storage SET used = 0 WHERE product_id = 1;
UPDATE seata_account.t_account SET balance = 1000 WHERE user_id = 1;
~~~



### 4.4 核心知识点总结

1. **一个核心注解**：`@GlobalTransactional` 仅需加在事务发起方的入口方法上
2. **一张必建表**：每个业务数据库都必须创建 `undo_log` 回滚日志表
3. **一组关键配置**：事务分组映射 + Seata Server 地址 + 数据源自动代理
4. **一个核心模式**：AT 模式业务代码零侵入，自动实现数据回滚，适配绝大多数业务场景

### 4.5 常见问题快速排查

- **连接不上 Seata Server**：检查 8091 端口是否通、`tx-service-group` 与 `vgroup-mapping` 是否对应
- **事务不回滚**：检查异常是否被 try-catch 吃掉、`undo_log`表是否存在、数据源代理是否开启
- **Feign 调用失败**：检查服务端口是否正确、被调用服务是否正常启动