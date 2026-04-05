package com.mochu.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.mochu")
@MapperScan("com.mochu.**.mapper")
@EnableAsync
public class MochuOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MochuOaApplication.class, args);
    }
}
