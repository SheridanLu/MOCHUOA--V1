package com.mochu.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan("com.mochu")
@EnableAsync
public class MochuOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MochuOaApplication.class, args);
    }
}
