package com.example.deepseekchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot 项目启动类
 * 整个项目的入口，运行main方法即可启动项目
 */
@SpringBootApplication // 核心注解：标识这是SpringBoot应用
public class DeepseekchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepseekchatApplication.class, args);
        System.out.println("========== DeepSeek聊天项目启动成功 ==========");
        System.out.println("访问地址：http://localhost:8080/index.html");
    }

}
