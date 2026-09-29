package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 整个电商系统唯一的启动入口。
 *
 * shopping-product
 * shopping-order
 * shopping-inventory
 * shopping-payment
 * 等模块本身都不是独立 Spring Boot 应用。
 *
 * bootstrap 模块通过 Maven 依赖把它们组合起来，
 * Spring Boot 启动后统一扫描。
 */
@SpringBootApplication
public class ShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingApplication.class, args);
    }
}
