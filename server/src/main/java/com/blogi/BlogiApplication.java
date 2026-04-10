package com.blogi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blogi.modules")
public class BlogiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogiApplication.class, args);
    }
}
