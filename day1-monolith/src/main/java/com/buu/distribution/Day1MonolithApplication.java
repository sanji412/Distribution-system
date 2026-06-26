package com.buu.distribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.buu.distribution.mapper")
@SpringBootApplication
public class Day1MonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(Day1MonolithApplication.class, args);
    }

}
