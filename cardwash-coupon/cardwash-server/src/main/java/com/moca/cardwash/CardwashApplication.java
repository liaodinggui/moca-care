package com.moca.cardwash;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 洗车优惠券系统启动类
 */
@SpringBootApplication
@MapperScan("com.moca.cardwash.mapper")
public class CardwashApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardwashApplication.class, args);
    }
}
