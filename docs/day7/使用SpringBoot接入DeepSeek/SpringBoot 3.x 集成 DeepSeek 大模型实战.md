# 《SpringBoot 3.x 集成 DeepSeek 大模型实战》课程文档

## 第 1 章 课程引入与项目概述

### 1.1 项目整体技术架构

本项目采用**前后端分离的极简架构**，技术栈全部为 Java 生态主流方案：

1. 后端技术栈
   - SpringBoot 3.2.12：项目基础框架，提供 Web 服务、依赖注入、配置管理
   - SpringAI：Spring 官方 AI 应用开发框架，统一抽象大模型调用接口
   - Spring AI OpenAI Starter：基于 Spring AI 规范，通过 OpenAI 兼容接口调用 DeepSeek 模型
   - Lombok：简化实体类代码，消除冗余的 get/set 方法
2. 前端技术栈
   - 原生 HTML/CSS/JavaScript：无需任何前端框架，降低学习门槛
   - Fetch API：浏览器原生发起异步请求，与后端接口交互
3. 第三方服务
   - DeepSeek 开放平台：提供大模型对话能力，通过标准 HTTP 接口调用

### 1.2 DeepSeek API 调用原理解析

调用大模型 API，本质就是一次标准的 HTTP POST 请求，和我们调用普通第三方接口没有区别。Spring AI 框架在底层帮助我们封装了这些 HTTP 细节，让我们只需要调用 Java 方法即可完成对话。

1. **请求地址**：`https://api.deepseek.com/v1/chat/completions`
2. **认证方式**：请求头中携带 `Authorization: Bearer 你的API密钥`
3. **请求参数**：核心是 messages消息列表，每条消息包含 role（角色）和 content（内容）
   - `system`：系统提示词，给 AI 设定人设、规则、边界
   - `user`：用户发送的问题
   - `assistant`：AI 的历史回复
4. **响应结果**：返回 JSON 格式数据，从中提取 AI 回复的文本内容

------

## 第 2 章 开发环境与准备工作

### 2.2 DeepSeek API Key 申请流程

【实操步骤】

1. 打开 DeepSeek 开放平台官网：[https://platform.deepseek.com/](https://link.wtturl.cn/?target=https%3A%2F%2Fplatform.deepseek.com%2F&scene=im&aid=497858&lang=zh)
2. 注册账号并完成实名认证
3. 进入「API 密钥管理」页面，创建新的 API Key
4. 复制保存密钥（仅显示一次，丢失需重新创建）

【安全提示】

API 密钥等同于账号密码，严禁提交到公开代码仓库、泄露给他人；生产环境需通过环境变量、配置中心等安全方式管理。

------

## 第 3 章 项目初始化与依赖配置 

### 3.1 创建 SpringBoot 项目

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
    <artifactId>deepseek-chat-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>deepseek-chat-demo</name>

    <properties>
        <java.version>21</java.version>
        <!-- Spring AI 最新稳定正式版，适配 Spring Boot 3.2.x -->
        <spring-ai.version>1.0.8</spring-ai.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 正式版标准 artifactId，功能与旧 starter 完全一致 -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

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

### 3.2 application.properties 配置文件

配置文件放在 `src/main/resources` 目录下，是项目的全局配置中心。

```properties
# 服务器端口配置
server.port=8080

# ========== Spring AI DeepSeek 配置 ==========
# API基础地址（使用官方地址，Spring AI 默认值就是官网，通常可不配）
spring.ai.openai.base-url=https://api.deepseek.com

# 【必填】替换为你自己申请的API密钥
spring.ai.openai.api-key=sk-你的API密钥

# 使用的对话模型名称（可选：deepseek-chat 或 deepseek-reasoner）
spring.ai.openai.chat.options.model=deepseek-chat

# 采样温度，默认0.7（取值范围0-2，越高越随机，越低越确定）
spring.ai.openai.chat.options.temperature=0.7
```



> 相比传统方式，我们**不再需要自定义超时配置**，Spring AI 内部已设置了合理的默认值（通常为 60 秒），足以应对大模型生成场景。

------

## 第 4 章 Spring AI 核心概念与自动配置

### 4.1 什么是 Spring AI？

Spring AI 是 Spring 官方推出的 AI 应用开发框架，它提供了一套**统一的、与具体模型无关的 API 抽象**。

- 核心思想：像操作数据库（JPA）一样，通过统一的接口操作不同的大模型（OpenAI、DeepSeek、Azure 等）。
- 优势：当我们需要更换大模型供应商时，只需更改配置和依赖，业务代码基本无需改动。

### 4.2 核心对象：ChatClient

`ChatClient` 是 Spring AI 中最核心的对话客户端，它采用**流式 API 风格（Fluent API）**，代码写起来像在写一句话：

```java
String reply = chatClient.prompt()
                .user("你好")
                .call()
                .content();
```



- `.prompt()`：开始构建一次提示
- `.user("...")`：设置用户消息
- `.call()`：发送请求并等待响应
- `.content()`：提取 AI 回复的文本内容

它完全屏蔽了底层的 JSON 组装、HTTP 发送、结果解析等繁琐步骤。

------

## 第 5 章 业务服务层核心实现

### 5.1 Service 层职责

Service 层是业务逻辑层，负责核心业务处理，承接 Controller 与 AI 模型。

- 注入 Spring AI 提供的 `ChatClient` 对象
- 调用其方法发送对话请求并返回结果
- 对上层 Controller 屏蔽底层调用细节

### 5.2 DeepSeekService 代码实现

位于 `service` 包下。

```java
package com.example.deepseekchat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * DeepSeek 对话业务服务
 * 核心职责：调用 Spring AI 的 ChatClient 完成对话
 */
@Service // 标识为业务服务类，交给Spring容器管理
public class DeepSeekService {

    private final ChatClient chatClient;

    /**
     * 构造器注入 ChatClient
     * 注意：Spring AI 会自动配置 ChatClient.Builder 并注入容器
     * 我们通过 Builder 构建出 ChatClient 实例
     */
    public DeepSeekService(ChatClient.Builder chatClientBuilder) {
        // 直接构建，无需额外配置，因为配置信息已在 application.properties 中指定
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 单轮对话方法
     * @param userMessage 用户输入的问题
     * @return AI回复的文本内容
     */
    public String chat(String userMessage) {
        // 使用 ChatClient 的流式 API 发送消息并获取回复
        // 这里我们保持简洁，不加 system 提示词，也可以按需添加
        return chatClient.prompt()
                .user(userMessage)          // 设置用户消息
                .call()                     // 执行调用
                .content();                 // 获取回复文本
    }

    /**
     * 带系统提示词的对话方法（可选扩展）
     * @param userMessage 用户问题
     * @param systemPrompt 系统提示词
     * @return AI回复
     */
    public String chatWithSystem(String userMessage, String systemPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)       // 设置系统提示词
                .user(userMessage)
                .call()
                .content();
    }
}
```

------

## 第 6 章 Controller 层与接口规范

### 6.1 Controller 层职责

Controller 层是控制层，负责接收前端请求、参数校验、调用 Service、返回结果，是项目对外的入口。

**注意**：由于我们修改了底层实现，但**前端交互接口保持不变**，因此 Controller 层代码完全无需改动！这充分体现了面向接口编程和解耦的优势。

### 6.2 ChatController 代码实现

位于 `controller` 包下。

```java
package com.example.deepseekchat.controller;

import com.example.deepseekchat.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天接口控制器
 * 提供前端可调用的HTTP接口
 */
@RestController // = @Controller + @ResponseBody，返回值自动转为JSON
@RequestMapping("/api/chat") // 接口统一访问前缀
@RequiredArgsConstructor
@CrossOrigin // 开启跨域支持，解决前后端端口不同的跨域问题
public class ChatController {

    // 注入业务服务
    private final DeepSeekService deepSeekService;

    /**
     * 对话接口
     * 请求方式：POST
     * 请求地址：/api/chat
     * @param requestMap 请求体，包含message字段
     * @return 统一格式的JSON结果
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> requestMap) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取参数
            String userMessage = requestMap.get("message");

            // 2. 参数校验
            if (userMessage == null || userMessage.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息内容不能为空");
                return result;
            }

            // 3. 调用业务层获取AI回复
            String reply = deepSeekService.chat(userMessage.trim());

            // 4. 封装成功结果
            result.put("success", true);
            result.put("reply", reply);

        } catch (Exception e) {
            // 异常捕获：打印日志，返回友好错误提示
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "调用AI失败：" + e.getMessage());
        }

        return result;
    }
}
```



------

## 附录一：项目完整目录结构

```tex
deepseek-chat-demo
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com
        │       └── example
        │           └── deepseekchat
        │               ├── DeepSeekChatApplication.java  // 启动类
        │               ├── controller
        │               │   └── ChatController.java       // 控制层
        │               └── service
        │                   └── DeepSeekService.java      // 业务层
        └── resources
            ├── application.properties                   // 配置文件
            └── static
                └── index.html                           // 前端页面
```

## 附录二：Maven 环境配置常见问题

### 问题 1：依赖下载速度慢

**解决方案**：在 Maven 的 `settings.xml` 中配置阿里云 HTTPS 镜像（无需关闭 HTTP 仓库安全拦截），完整配置示例如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd">

    <!-- 自定义本地仓库路径（可根据自身需求修改） -->
    <localRepository>E:\MavenRepository</localRepository>

    <mirrors>
        <!-- 保留 Maven 默认的 HTTP 仓库安全拦截（无需注释） -->
        <mirror>
            <id>maven-default-http-blocker</id>
            <mirrorOf>external:http:*</mirrorOf>
            <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
            <url>http://0.0.0.0/</url>
            <blocked>true</blocked>
        </mirror>

        <!-- 阿里云公共镜像：仅覆盖中央仓库，HTTPS协议安全可用 -->
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>central</mirrorOf>
            <name>阿里云公共仓库</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>

    <profiles></profiles>
    <activeProfiles></activeProfiles>
</settings>
```

`settings.xml` 文件位置参考：

| 操作系统      | settings.xml 路径                      |
| ------------- | -------------------------------------- |
| Windows       | `C:\Users\你的用户名\.m2\settings.xml` |
| macOS / Linux | `~/.m2/settings.xml`                   |

### 问题 2：报错 "Blocked mirror for repositories"

**原因**：Maven 3.8.1+ 默认阻止 HTTP 仓库访问（安全机制）。

**解决方案**：无需关闭该拦截，确保使用的镜像地址为 HTTPS 协议（如上述阿里云镜像）即可，该错误会自动消失。

### 问题 3：依赖下载失败，提示 "failed to transfer"

**原因**：本地仓库缓存了之前失败的下载记录，或本地仓库路径配置错误。

**解决方案**：

1. 查看本地仓库路径：打开 `settings.xml`，找到 `<localRepository>` 标签的配置值；
2. 进入本地仓库目录，删除 `org/springframework/ai` 整个文件夹；
3. 在 IDEA 中点击 Maven 面板的刷新按钮，同时勾选 **"Force Update Snapshots"**。

### 问题 4：如何在 IDEA 中定位 settings.xml

1. 打开 IntelliJ IDEA，点击 **File → Settings**
2. 导航到 **Build, Execution, Deployment → Build Tools → Maven**
3. 在右侧面板中，**User settings file** 一栏显示的就是当前使用的 `settings.xml` 路径

------

## 附录三：常见问题 FAQ

**Q：启动时报错 "No qualifying bean of type ChatClient.Builder"**

A：检查是否添加了 `spring-ai-openai-spring-boot-starter` 依赖，并确认 `application.properties` 中配置了有效的 `spring.ai.openai.api-key` 和正确的 `spring.ai.openai.base-url`（包含 /v1 路径）。

**Q：调用接口返回 "Connection refused"**

A：检查网络是否能够访问 `https://api.deepseek.com/v1`，确认 API Key 是否有效，且未被封禁。

**Q：AI 回复速度很慢**

A：大模型生成需要时间，这是正常现象。Spring AI 默认超时时间为 60 秒，如果超时可通过 `spring.ai.openai.chat.options.timeout` 配置调整（单位：毫秒）。

**Q：如何更换其他大模型？**

A：只需修改 `application.properties` 中的 `base-url`、`api-key` 和 `model` 参数即可，业务代码无需改动。

**Q：生产环境中跨域配置如何优化？**

A：将 `@CrossOrigin` 注解改为指定允许的前端域名，例如：`@CrossOrigin(origins = {"https://your-frontend.com", "https://admin.your-frontend.com"})`，避免允许所有域名访问。以及 `model` 参数即可，业务代码无需改动。
