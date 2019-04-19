package com.hand.oauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hand.oauth.domain.mapper")
public class OauthApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(OauthApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("do something here ...");
    }
}
