package com.blogi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({
    "com.blogi.modules.auth.mapper",
    "com.blogi.modules.post.mapper",
    "com.blogi.modules.settings.mapper",
    "com.blogi.modules.visitor.mapper",
})
public class BlogiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogiApplication.class, args);
    }
}
