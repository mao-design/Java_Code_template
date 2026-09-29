package com.example.ratelimiting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.ratelimiting.mapper")
public class ApiRateLimitingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiRateLimitingApplication.class, args);
    }
}
